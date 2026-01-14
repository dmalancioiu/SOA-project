FROM node:18-alpine

WORKDIR /home/app

# Install system dependencies
RUN apk add --no-cache curl

# Copy package files
COPY package*.json ./

# Install Node dependencies
RUN npm ci --only=production

# Copy function code
COPY handler.js .

# Health check
HEALTHCHECK --interval=5s --timeout=3s --start-period=10s --retries=3 \
    CMD curl -f http://localhost:8080/health || exit 1

# Expose port
EXPOSE 8080

# Run the function
CMD ["node", "handler.js"]
