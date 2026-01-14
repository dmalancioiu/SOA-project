-- Food Delivery Platform Database Initialization

CREATE DATABASE IF NOT EXISTS fooddelivery CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE fooddelivery;

-- Grant permissions
GRANT ALL PRIVILEGES ON fooddelivery.* TO 'fooddelivery_user'@'%';
FLUSH PRIVILEGES;

-- Note: Tables will be created automatically by JPA/Hibernate from each microservice
