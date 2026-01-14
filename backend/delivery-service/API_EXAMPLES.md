# Delivery Service API Examples

## Quick Start Examples

All examples assume the service is running on `http://localhost:8084`

---

## 1. Assign Driver to Order

**Endpoint:** `POST /deliveries/assign`

**Description:** Assign a driver to an order and create a delivery record with initial status ASSIGNED.

**Request:**
```bash
curl -X POST http://localhost:8084/deliveries/assign \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": 1,
    "driverId": 100,
    "pickupAddress": "123 Main Street, Restaurant City",
    "deliveryAddress": "456 Oak Avenue, Customer Town"
  }'
```

**Request Body (JSON):**
```json
{
  "orderId": 1,
  "driverId": 100,
  "pickupAddress": "123 Main Street, Restaurant City",
  "deliveryAddress": "456 Oak Avenue, Customer Town"
}
```

**Success Response (201 Created):**
```json
{
  "id": 1,
  "orderId": 1,
  "driverId": 100,
  "pickupAddress": "123 Main Street, Restaurant City",
  "deliveryAddress": "456 Oak Avenue, Customer Town",
  "status": "ASSIGNED",
  "estimatedDeliveryTime": "2026-01-13T10:30:45.123456",
  "actualDeliveryTime": null,
  "driverLat": null,
  "driverLng": null,
  "createdAt": "2026-01-13T10:00:45.123456",
  "updatedAt": "2026-01-13T10:00:45.123456"
}
```

**Error Response (400 Bad Request - Missing Field):**
```json
{
  "timestamp": "2026-01-13T10:00:45.123456",
  "status": 400,
  "message": "Validation failed",
  "errors": {
    "driverId": "Driver ID cannot be null",
    "pickupAddress": "Pickup address cannot be blank"
  }
}
```

---

## 2. Get Delivery by ID

**Endpoint:** `GET /deliveries/{id}`

**Description:** Retrieve details of a specific delivery.

**Request:**
```bash
curl http://localhost:8084/deliveries/1
```

**Success Response (200 OK):**
```json
{
  "id": 1,
  "orderId": 1,
  "driverId": 100,
  "pickupAddress": "123 Main Street, Restaurant City",
  "deliveryAddress": "456 Oak Avenue, Customer Town",
  "status": "ASSIGNED",
  "estimatedDeliveryTime": "2026-01-13T10:30:45.123456",
  "actualDeliveryTime": null,
  "driverLat": null,
  "driverLng": null,
  "createdAt": "2026-01-13T10:00:45.123456",
  "updatedAt": "2026-01-13T10:00:45.123456"
}
```

**Error Response (404 Not Found):**
```json
{
  "timestamp": "2026-01-13T10:01:45.123456",
  "status": 404,
  "message": "Delivery not found with id: 999"
}
```

---

## 3. Get Delivery by Order ID

**Endpoint:** `GET /deliveries/order/{orderId}`

**Description:** Find the delivery associated with a specific order.

**Request:**
```bash
curl http://localhost:8084/deliveries/order/1
```

**Success Response (200 OK):**
```json
{
  "id": 1,
  "orderId": 1,
  "driverId": 100,
  "pickupAddress": "123 Main Street, Restaurant City",
  "deliveryAddress": "456 Oak Avenue, Customer Town",
  "status": "ASSIGNED",
  "estimatedDeliveryTime": "2026-01-13T10:30:45.123456",
  "actualDeliveryTime": null,
  "driverLat": null,
  "driverLng": null,
  "createdAt": "2026-01-13T10:00:45.123456",
  "updatedAt": "2026-01-13T10:00:45.123456"
}
```

---

## 4. Get Deliveries by Driver ID

**Endpoint:** `GET /deliveries/driver/{driverId}`

**Description:** Get all deliveries assigned to a specific driver.

**Request:**
```bash
curl http://localhost:8084/deliveries/driver/100
```

**Success Response (200 OK):**
```json
[
  {
    "id": 1,
    "orderId": 1,
    "driverId": 100,
    "pickupAddress": "123 Main Street, Restaurant City",
    "deliveryAddress": "456 Oak Avenue, Customer Town",
    "status": "EN_ROUTE_TO_RESTAURANT",
    "estimatedDeliveryTime": "2026-01-13T10:30:45.123456",
    "actualDeliveryTime": null,
    "driverLat": 40.7128,
    "driverLng": -74.0060,
    "createdAt": "2026-01-13T10:00:45.123456",
    "updatedAt": "2026-01-13T10:05:30.123456"
  },
  {
    "id": 2,
    "orderId": 2,
    "driverId": 100,
    "pickupAddress": "789 Pizza Place, Food City",
    "deliveryAddress": "321 Home Street, Residential Area",
    "status": "ASSIGNED",
    "estimatedDeliveryTime": "2026-01-13T11:00:00.123456",
    "actualDeliveryTime": null,
    "driverLat": null,
    "driverLng": null,
    "createdAt": "2026-01-13T10:45:00.123456",
    "updatedAt": "2026-01-13T10:45:00.123456"
  }
]
```

---

## 5. Update Delivery Status

**Endpoint:** `PUT /deliveries/{id}/status`

**Description:** Update the status of a delivery. When status changes to DELIVERED, the service automatically:
- Sets the actual delivery time
- Calls FaaS function for analytics
- Publishes event to Kafka

**Status Values:** ASSIGNED, EN_ROUTE_TO_RESTAURANT, PICKED_UP, EN_ROUTE_TO_CUSTOMER, DELIVERED, FAILED

**Request - Transition to EN_ROUTE_TO_RESTAURANT:**
```bash
curl -X PUT "http://localhost:8084/deliveries/1/status?status=EN_ROUTE_TO_RESTAURANT"
```

**Success Response (200 OK):**
```json
{
  "id": 1,
  "orderId": 1,
  "driverId": 100,
  "pickupAddress": "123 Main Street, Restaurant City",
  "deliveryAddress": "456 Oak Avenue, Customer Town",
  "status": "EN_ROUTE_TO_RESTAURANT",
  "estimatedDeliveryTime": "2026-01-13T10:30:45.123456",
  "actualDeliveryTime": null,
  "driverLat": 40.7128,
  "driverLng": -74.0060,
  "createdAt": "2026-01-13T10:00:45.123456",
  "updatedAt": "2026-01-13T10:05:30.123456"
}
```

**Request - Transition to PICKED_UP:**
```bash
curl -X PUT "http://localhost:8084/deliveries/1/status?status=PICKED_UP"
```

**Response:**
```json
{
  "id": 1,
  "orderId": 1,
  "driverId": 100,
  "pickupAddress": "123 Main Street, Restaurant City",
  "deliveryAddress": "456 Oak Avenue, Customer Town",
  "status": "PICKED_UP",
  "estimatedDeliveryTime": "2026-01-13T10:30:45.123456",
  "actualDeliveryTime": null,
  "driverLat": 40.7128,
  "driverLng": -74.0060,
  "createdAt": "2026-01-13T10:00:45.123456",
  "updatedAt": "2026-01-13T10:10:15.123456"
}
```

**Request - Transition to EN_ROUTE_TO_CUSTOMER:**
```bash
curl -X PUT "http://localhost:8084/deliveries/1/status?status=EN_ROUTE_TO_CUSTOMER"
```

**Response:**
```json
{
  "id": 1,
  "orderId": 1,
  "driverId": 100,
  "pickupAddress": "123 Main Street, Restaurant City",
  "deliveryAddress": "456 Oak Avenue, Customer Town",
  "status": "EN_ROUTE_TO_CUSTOMER",
  "estimatedDeliveryTime": "2026-01-13T10:30:45.123456",
  "actualDeliveryTime": null,
  "driverLat": 40.7135,
  "driverLng": -74.0070,
  "createdAt": "2026-01-13T10:00:45.123456",
  "updatedAt": "2026-01-13T10:15:00.123456"
}
```

**Request - Mark as DELIVERED (Triggers FaaS Analytics):**
```bash
curl -X PUT "http://localhost:8084/deliveries/1/status?status=DELIVERED"
```

**Response:**
```json
{
  "id": 1,
  "orderId": 1,
  "driverId": 100,
  "pickupAddress": "123 Main Street, Restaurant City",
  "deliveryAddress": "456 Oak Avenue, Customer Town",
  "status": "DELIVERED",
  "estimatedDeliveryTime": "2026-01-13T10:30:45.123456",
  "actualDeliveryTime": "2026-01-13T10:25:30.123456",
  "driverLat": 40.7145,
  "driverLng": -74.0080,
  "createdAt": "2026-01-13T10:00:45.123456",
  "updatedAt": "2026-01-13T10:25:30.123456"
}
```

**Note:** When status changes to DELIVERED:
1. actualDeliveryTime is automatically set to current timestamp
2. FaaS analytics function is called with delivery analytics
3. DELIVERY_STATUS_CHANGED event is published to Kafka topic "delivery.events"

---

## 6. Update Driver Location

**Endpoint:** `PUT /deliveries/{id}/location`

**Description:** Update the real-time location of the driver. This publishes a LOCATION_UPDATED event to Kafka.

**Request:**
```bash
curl -X PUT http://localhost:8084/deliveries/1/location \
  -H "Content-Type: application/json" \
  -d '{
    "latitude": 40.7128,
    "longitude": -74.0060
  }'
```

**Request Body (JSON):**
```json
{
  "latitude": 40.7128,
  "longitude": -74.0060
}
```

**Success Response (200 OK):**
```json
{
  "id": 1,
  "orderId": 1,
  "driverId": 100,
  "pickupAddress": "123 Main Street, Restaurant City",
  "deliveryAddress": "456 Oak Avenue, Customer Town",
  "status": "EN_ROUTE_TO_CUSTOMER",
  "estimatedDeliveryTime": "2026-01-13T10:30:45.123456",
  "actualDeliveryTime": null,
  "driverLat": 40.7128,
  "driverLng": -74.0060,
  "createdAt": "2026-01-13T10:00:45.123456",
  "updatedAt": "2026-01-13T10:20:30.123456"
}
```

**Error Response (400 Bad Request - Invalid Coordinates):**
```json
{
  "timestamp": "2026-01-13T10:20:30.123456",
  "status": 400,
  "message": "Validation failed",
  "errors": {
    "latitude": "Latitude cannot be null",
    "longitude": "Longitude cannot be null"
  }
}
```

---

## 7. Get Deliveries by Status

**Endpoint:** `GET /deliveries/status/{status}`

**Description:** Retrieve all deliveries with a specific status.

**Status Values:** ASSIGNED, EN_ROUTE_TO_RESTAURANT, PICKED_UP, EN_ROUTE_TO_CUSTOMER, DELIVERED, FAILED

**Request - Get all assigned deliveries:**
```bash
curl http://localhost:8084/deliveries/status/ASSIGNED
```

**Success Response (200 OK):**
```json
[
  {
    "id": 5,
    "orderId": 5,
    "driverId": 101,
    "pickupAddress": "999 Burger Joint, Food City",
    "deliveryAddress": "555 Park Lane, Suburb",
    "status": "ASSIGNED",
    "estimatedDeliveryTime": "2026-01-13T11:00:00.123456",
    "actualDeliveryTime": null,
    "driverLat": null,
    "driverLng": null,
    "createdAt": "2026-01-13T10:30:00.123456",
    "updatedAt": "2026-01-13T10:30:00.123456"
  },
  {
    "id": 6,
    "orderId": 6,
    "driverId": 102,
    "pickupAddress": "555 Chinese Restaurant, Downtown",
    "deliveryAddress": "777 Maple Drive, North District",
    "status": "ASSIGNED",
    "estimatedDeliveryTime": "2026-01-13T11:15:00.123456",
    "actualDeliveryTime": null,
    "driverLat": null,
    "driverLng": null,
    "createdAt": "2026-01-13T10:45:00.123456",
    "updatedAt": "2026-01-13T10:45:00.123456"
  }
]
```

**Request - Get all delivered deliveries:**
```bash
curl http://localhost:8084/deliveries/status/DELIVERED
```

**Success Response (200 OK):**
```json
[
  {
    "id": 1,
    "orderId": 1,
    "driverId": 100,
    "pickupAddress": "123 Main Street, Restaurant City",
    "deliveryAddress": "456 Oak Avenue, Customer Town",
    "status": "DELIVERED",
    "estimatedDeliveryTime": "2026-01-13T10:30:45.123456",
    "actualDeliveryTime": "2026-01-13T10:25:30.123456",
    "driverLat": 40.7145,
    "driverLng": -74.0080,
    "createdAt": "2026-01-13T10:00:45.123456",
    "updatedAt": "2026-01-13T10:25:30.123456"
  }
]
```

**Request - Get all failed deliveries:**
```bash
curl http://localhost:8084/deliveries/status/FAILED
```

---

## Kafka Events Published

### Event 1: DELIVERY_ASSIGNED
**When:** Driver is assigned to an order
**Topic:** delivery.events

```json
{
  "deliveryId": 1,
  "orderId": 1,
  "driverId": 100,
  "status": "ASSIGNED",
  "eventType": "DELIVERY_ASSIGNED",
  "timestamp": "2026-01-13T10:00:45.123456"
}
```

### Event 2: DELIVERY_STATUS_CHANGED
**When:** Delivery status is updated
**Topic:** delivery.events

```json
{
  "deliveryId": 1,
  "orderId": 1,
  "driverId": 100,
  "status": "DELIVERED",
  "eventType": "DELIVERY_STATUS_CHANGED",
  "timestamp": "2026-01-13T10:25:30.123456"
}
```

### Event 3: LOCATION_UPDATED
**When:** Driver location is updated
**Topic:** delivery.events

```json
{
  "deliveryId": 1,
  "orderId": 1,
  "driverId": 100,
  "status": "EN_ROUTE_TO_CUSTOMER",
  "eventType": "LOCATION_UPDATED",
  "timestamp": "2026-01-13T10:20:30.123456"
}
```

---

## FaaS Integration

### Analytics Function Call
**When:** Delivery status changes to DELIVERED
**Method:** POST
**Endpoint:** `{FAAS_GATEWAY_URL}/api/v1/delivery-analytics`

**Request Payload:**
```json
{
  "deliveryId": "1",
  "orderId": "1",
  "driverId": "100",
  "deliveryTime": "2026-01-13T10:25:30.123456"
}
```

---

## Health Check

**Endpoint:** `GET /actuator/health`

**Request:**
```bash
curl http://localhost:8084/actuator/health
```

**Response (200 OK):**
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "MySQL"
      }
    },
    "redis": {
      "status": "UP"
    },
    "kafka": {
      "status": "UP"
    }
  }
}
```

---

## Common HTTP Status Codes

| Status | Meaning | Example |
|--------|---------|---------|
| 200 | OK | Successful GET, PUT |
| 201 | Created | Successful POST (driver assignment) |
| 400 | Bad Request | Validation error |
| 404 | Not Found | Delivery not found |
| 500 | Server Error | Unexpected error |

---

## Error Response Format

All error responses follow this format:

```json
{
  "timestamp": "2026-01-13T10:00:45.123456",
  "status": 400,
  "message": "Validation failed",
  "errors": {
    "fieldName": "Error message"
  }
}
```

---

## Testing Flow

**Complete delivery workflow:**

1. Assign driver:
   ```bash
   curl -X POST http://localhost:8084/deliveries/assign -H "Content-Type: application/json" -d '{"orderId":1,"driverId":100,"pickupAddress":"Rest","deliveryAddress":"Home"}'
   ```

2. Update to EN_ROUTE_TO_RESTAURANT:
   ```bash
   curl -X PUT "http://localhost:8084/deliveries/1/status?status=EN_ROUTE_TO_RESTAURANT"
   ```

3. Update location:
   ```bash
   curl -X PUT http://localhost:8084/deliveries/1/location -H "Content-Type: application/json" -d '{"latitude":40.7128,"longitude":-74.0060}'
   ```

4. Update to PICKED_UP:
   ```bash
   curl -X PUT "http://localhost:8084/deliveries/1/status?status=PICKED_UP"
   ```

5. Update location again:
   ```bash
   curl -X PUT http://localhost:8084/deliveries/1/location -H "Content-Type: application/json" -d '{"latitude":40.7145,"longitude":-74.0080}'
   ```

6. Update to EN_ROUTE_TO_CUSTOMER:
   ```bash
   curl -X PUT "http://localhost:8084/deliveries/1/status?status=EN_ROUTE_TO_CUSTOMER"
   ```

7. Mark as DELIVERED:
   ```bash
   curl -X PUT "http://localhost:8084/deliveries/1/status?status=DELIVERED"
   ```

8. Verify delivery:
   ```bash
   curl http://localhost:8084/deliveries/1
   ```

---

**Note:** All timestamps are in ISO 8601 format (YYYY-MM-DDTHH:mm:ss.SSSSSS)
