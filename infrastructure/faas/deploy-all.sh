#!/bin/bash

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
REGISTRY=${DOCKER_REGISTRY:-""}
OPENFAAS_URL=${OPENFAAS_URL:-"http://localhost:8080"}
OPENFAAS_USER=${OPENFAAS_USER:-"admin"}
OPENFAAS_PASSWORD=${OPENFAAS_PASSWORD:-""}
NAMESPACE=${FAAS_NAMESPACE:-"openfaas-fn"}
PARALLEL_DEPLOY=${PARALLEL_DEPLOY:-false}

# Script directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Functions
log() {
    echo -e "${BLUE}[$(date +'%Y-%m-%d %H:%M:%S')]${NC} $1"
}

success() {
    echo -e "${GREEN}[✓]${NC} $1"
}

error() {
    echo -e "${RED}[✗]${NC} $1"
}

warn() {
    echo -e "${YELLOW}[!]${NC} $1"
}

usage() {
    cat << EOF
Usage: $0 [OPTIONS]

Deploy OpenFaaS functions for Food Delivery Platform

OPTIONS:
    -h, --help              Show this help message
    -u, --url URL           OpenFaaS gateway URL (default: http://localhost:8080)
    -U, --user USER         OpenFaaS username (default: admin)
    -P, --password PASSWORD OpenFaaS password
    -n, --namespace NS      Kubernetes namespace (default: openfaas-fn)
    -r, --registry REGISTRY Docker registry URL
    -p, --parallel          Deploy functions in parallel
    -d, --dry-run           Show what would be deployed without deploying
    -c, --clean             Remove all deployed functions first
    -b, --build-only        Build Docker images only, don't deploy

ENVIRONMENT VARIABLES:
    OPENFAAS_URL            OpenFaaS gateway URL
    OPENFAAS_USER           OpenFaaS username
    OPENFAAS_PASSWORD       OpenFaaS password
    DOCKER_REGISTRY         Docker registry URL
    FAAS_NAMESPACE          Kubernetes namespace
    PARALLEL_DEPLOY         Deploy in parallel (true/false)

EXAMPLES:
    # Deploy all functions to local OpenFaaS
    $0

    # Deploy with custom registry and authentication
    $0 -u http://faas.example.com -U admin -P password -r myregistry.com

    # Dry-run to see what would be deployed
    $0 --dry-run

    # Build images only
    $0 --build-only

EOF
    exit 0
}

# Parse arguments
DRY_RUN=false
CLEAN_FIRST=false
BUILD_ONLY=false

while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--help)
            usage
            ;;
        -u|--url)
            OPENFAAS_URL="$2"
            shift 2
            ;;
        -U|--user)
            OPENFAAS_USER="$2"
            shift 2
            ;;
        -P|--password)
            OPENFAAS_PASSWORD="$2"
            shift 2
            ;;
        -n|--namespace)
            NAMESPACE="$2"
            shift 2
            ;;
        -r|--registry)
            REGISTRY="$2"
            shift 2
            ;;
        -p|--parallel)
            PARALLEL_DEPLOY=true
            shift
            ;;
        -d|--dry-run)
            DRY_RUN=true
            shift
            ;;
        -c|--clean)
            CLEAN_FIRST=true
            shift
            ;;
        -b|--build-only)
            BUILD_ONLY=true
            shift
            ;;
        *)
            error "Unknown option: $1"
            usage
            ;;
    esac
done

# Function to check prerequisites
check_prerequisites() {
    log "Checking prerequisites..."

    # Check Docker
    if ! command -v docker &> /dev/null; then
        error "Docker is not installed"
        exit 1
    fi
    success "Docker found: $(docker --version)"

    # Check faas-cli
    if ! command -v faas-cli &> /dev/null; then
        error "faas-cli is not installed"
        echo "Install with: curl -sSL https://cli.openfaas.com | sh"
        exit 1
    fi
    success "faas-cli found: $(faas-cli version)"

    # Check kubectl if deploying to Kubernetes
    if [ -f "${SCRIPT_DIR}/stack.yml" ]; then
        if ! command -v kubectl &> /dev/null; then
            warn "kubectl not found - will use HTTP API instead"
        else
            success "kubectl found: $(kubectl version --short 2>/dev/null | head -1 || echo 'installed')"
        fi
    fi
}

# Function to build Docker image
build_image() {
    local func_name=$1
    local func_dir="${SCRIPT_DIR}/${func_name}"
    local image_name=$func_name

    if [ -n "$REGISTRY" ]; then
        image_name="${REGISTRY}/${func_name}"
    fi

    if [ ! -d "$func_dir" ]; then
        error "Function directory not found: $func_dir"
        return 1
    fi

    if [ ! -f "${func_dir}/Dockerfile" ]; then
        error "Dockerfile not found in $func_dir"
        return 1
    fi

    log "Building Docker image for $func_name..."

    if [ "$DRY_RUN" = true ]; then
        log "[DRY-RUN] Would execute: docker build -t ${image_name}:latest ${func_dir}"
        return 0
    fi

    if docker build -t "${image_name}:latest" "$func_dir"; then
        success "Image built successfully: ${image_name}:latest"
        return 0
    else
        error "Failed to build image for $func_name"
        return 1
    fi
}

# Function to push Docker image
push_image() {
    local func_name=$1
    local image_name=$func_name

    if [ -n "$REGISTRY" ]; then
        image_name="${REGISTRY}/${func_name}"
    fi

    if [ -z "$REGISTRY" ]; then
        log "Skipping push (no registry specified)"
        return 0
    fi

    log "Pushing Docker image: ${image_name}:latest..."

    if [ "$DRY_RUN" = true ]; then
        log "[DRY-RUN] Would execute: docker push ${image_name}:latest"
        return 0
    fi

    if docker push "${image_name}:latest"; then
        success "Image pushed successfully: ${image_name}:latest"
        return 0
    else
        error "Failed to push image for $func_name"
        return 1
    fi
}

# Function to deploy function with OpenFaaS
deploy_function() {
    local func_name=$1
    local func_dir="${SCRIPT_DIR}/${func_name}"
    local stack_file="${func_dir}/${func_name}.yml"

    if [ ! -f "$stack_file" ]; then
        error "Stack file not found: $stack_file"
        return 1
    fi

    log "Deploying function: $func_name..."

    if [ "$DRY_RUN" = true ]; then
        log "[DRY-RUN] Would deploy from: $stack_file"
        log "[DRY-RUN] Stack file content:"
        head -20 "$stack_file" | sed 's/^/  /'
        return 0
    fi

    # Export environment variables for stack file
    export OPENFAAS_URL
    export OPENFAAS_USER
    export OPENFAAS_PASSWORD
    export DOCKER_REGISTRY=$REGISTRY

    # Deploy using faas-cli
    if faas-cli deploy -f "$stack_file" \
        --gateway "${OPENFAAS_URL}" \
        --username "${OPENFAAS_USER}" \
        --password "${OPENFAAS_PASSWORD}" \
        --namespace "${NAMESPACE}"; then
        success "Function deployed: $func_name"
        return 0
    else
        error "Failed to deploy function: $func_name"
        return 1
    fi
}

# Function to remove function
remove_function() {
    local func_name=$1

    log "Removing function: $func_name..."

    if [ "$DRY_RUN" = true ]; then
        log "[DRY-RUN] Would remove function: $func_name"
        return 0
    fi

    if faas-cli remove "$func_name" \
        --gateway "${OPENFAAS_URL}" \
        --username "${OPENFAAS_USER}" \
        --password "${OPENFAAS_PASSWORD}" \
        --namespace "${NAMESPACE}"; then
        success "Function removed: $func_name"
        return 0
    else
        warn "Failed to remove function: $func_name (may not exist)"
        return 0
    fi
}

# Function to test deployment
test_function() {
    local func_name=$1
    local url="${OPENFAAS_URL}/function/${func_name}/health"

    log "Testing function: $func_name..."

    if [ "$DRY_RUN" = true ]; then
        log "[DRY-RUN] Would test: curl $url"
        return 0
    fi

    if curl -sf "$url" > /dev/null; then
        success "Function is healthy: $func_name"
        return 0
    else
        warn "Health check failed for: $func_name (may still be starting)"
        return 1
    fi
}

# Main deployment logic
main() {
    local functions=("delivery-analytics" "order-completion" "auto-close-orders")
    local failed_functions=()
    local success_count=0

    log "========================================"
    log "OpenFaaS Function Deployment Script"
    log "========================================"

    if [ "$DRY_RUN" = true ]; then
        warn "DRY-RUN MODE ENABLED"
    fi

    log "Configuration:"
    log "  OpenFaaS URL: $OPENFAAS_URL"
    log "  Namespace: $NAMESPACE"
    log "  Registry: ${REGISTRY:-'none (local)'}"
    log "  Parallel: $PARALLEL_DEPLOY"
    echo ""

    # Check prerequisites
    check_prerequisites
    echo ""

    # Clean up if requested
    if [ "$CLEAN_FIRST" = true ]; then
        log "Removing previously deployed functions..."
        for func in "${functions[@]}"; do
            remove_function "$func"
        done
        echo ""
    fi

    # Build images
    log "Building Docker images..."
    for func in "${functions[@]}"; do
        if ! build_image "$func"; then
            failed_functions+=("$func")
        fi
    done

    if [ ${#failed_functions[@]} -gt 0 ]; then
        error "Failed to build images for: ${failed_functions[*]}"
        exit 1
    fi
    echo ""

    # Push images if registry is specified
    if [ -n "$REGISTRY" ]; then
        log "Pushing Docker images..."
        for func in "${functions[@]}"; do
            push_image "$func"
        done
        echo ""
    fi

    # Stop here if build-only
    if [ "$BUILD_ONLY" = true ]; then
        success "Build completed successfully"
        exit 0
    fi

    # Deploy functions
    log "Deploying functions..."
    failed_functions=()

    if [ "$PARALLEL_DEPLOY" = true ]; then
        for func in "${functions[@]}"; do
            deploy_function "$func" &
        done
        wait
    else
        for func in "${functions[@]}"; do
            if deploy_function "$func"; then
                success_count=$((success_count + 1))
            else
                failed_functions+=("$func")
            fi
        done
    fi
    echo ""

    # Test deployments
    if [ "$DRY_RUN" = false ]; then
        log "Testing deployments (waiting for functions to start)..."
        sleep 5
        for func in "${functions[@]}"; do
            test_function "$func"
        done
        echo ""
    fi

    # Summary
    log "========================================"
    if [ ${#failed_functions[@]} -eq 0 ]; then
        success "All functions deployed successfully!"
        log "Functions deployed: ${functions[*]}"
        log "Gateway URL: $OPENFAAS_URL"
        echo ""
        log "Test a function:"
        log "  curl -X POST ${OPENFAAS_URL}/function/delivery-analytics -d '{...}'"
    else
        error "Deployment completed with errors"
        error "Failed functions: ${failed_functions[*]}"
        exit 1
    fi
}

# Run main function
main "$@"
