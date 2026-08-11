-- Maahish baseline schema (exported from JPA/Hibernate working schema).
-- Fresh MySQL 8+ databases: Flyway V1 creates all tables; prod uses ddl-auto=validate.
-- Do not edit lightly — run Hibernate validate after changes.

-- MySQL dump 10.13  Distrib 8.0.41, for Win64 (x86_64)
--
-- Host: localhost    Database: maahish_db
-- ------------------------------------------------------
-- Server version	8.0.41

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `addresses`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `addresses` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `address_line` varchar(500) NOT NULL,
  `alternate_mobile` varchar(15) DEFAULT NULL,
  `city` varchar(100) NOT NULL,
  `country` varchar(100) NOT NULL,
  `country_code` varchar(2) NOT NULL,
  `district` varchar(100) DEFAULT NULL,
  `full_name` varchar(100) NOT NULL,
  `is_default` bit(1) NOT NULL,
  `landmark` varchar(255) DEFAULT NULL,
  `mobile` varchar(15) NOT NULL,
  `pincode` varchar(10) NOT NULL,
  `state` varchar(100) NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK1fa36y2oqhao3wgg2rw1pi459` (`user_id`),
  CONSTRAINT `FK1fa36y2oqhao3wgg2rw1pi459` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `cart_items`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cart_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `quantity` int NOT NULL,
  `cart_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_cart_product` (`cart_id`,`product_id`),
  KEY `FK1re40cjegsfvw58xrkdp6bac6` (`product_id`),
  CONSTRAINT `FK1re40cjegsfvw58xrkdp6bac6` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`),
  CONSTRAINT `FKpcttvuq4mxppo8sxggjtn5i2c` FOREIGN KEY (`cart_id`) REFERENCES `carts` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `carts`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `carts` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK64t7ox312pqal3p7fg9o503c2` (`user_id`),
  CONSTRAINT `FKb5o626f86h46m4s7ms6ginnop` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `categories`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categories` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `active` bit(1) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `image_url` varchar(500) DEFAULT NULL,
  `name` varchar(100) NOT NULL,
  `slug` varchar(120) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKt8o6pivur7nn124jehx7cygw5` (`name`),
  UNIQUE KEY `UKoul14ho7bctbefv8jywp5v3i2` (`slug`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `commission_rules`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `commission_rules` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `commission_percentage` decimal(5,2) NOT NULL,
  `enabled` bit(1) NOT NULL,
  `name` varchar(150) NOT NULL,
  `category_id` bigint DEFAULT NULL,
  `fabric_type_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_commission_fabric_type` (`fabric_type_id`),
  KEY `idx_commission_category` (`category_id`),
  KEY `idx_commission_enabled` (`enabled`),
  CONSTRAINT `FK6j37uf2u5wnyqichk5d4gsr4r` FOREIGN KEY (`fabric_type_id`) REFERENCES `fabric_types` (`id`),
  CONSTRAINT `FKine9mtdktv8fx614439yo68y2` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `fabric_types`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fabric_types` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `active` bit(1) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `name` varchar(100) NOT NULL,
  `slug` varchar(120) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKia0muji63otte44gyer4suk4g` (`name`),
  UNIQUE KEY `UKd1imbfpolbyyhia7lutnho7ah` (`slug`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `notification_preferences`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notification_preferences` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `email_enabled` bit(1) NOT NULL,
  `in_app_enabled` bit(1) NOT NULL,
  `sms_enabled` bit(1) NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_notification_pref_user` (`user_id`),
  CONSTRAINT `FKt9qjvmcl36i14utm5uptyqg84` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `notifications`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notifications` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `action_path` varchar(255) DEFAULT NULL,
  `channel` enum('EMAIL','IN_APP','PUSH','SMS','WHATSAPP') NOT NULL,
  `message` varchar(1000) NOT NULL,
  `read_flag` bit(1) NOT NULL,
  `reference_id` bigint DEFAULT NULL,
  `reference_type` enum('ORDER','PRODUCT','RETURN','SELLER','USER') DEFAULT NULL,
  `title` varchar(200) NOT NULL,
  `type` enum('CUSTOMER_SHIPPED_RETURN','EXCHANGE_COMPLETED','EXCHANGE_PENDING','EXCHANGE_REQUESTED','EXCHANGE_SHIPPED','NEW_ORDER','NEW_RETURN_REQUEST','NEW_USER_REGISTERED','ORDER_CANCELLED','ORDER_CONFIRMED','ORDER_DELIVERED','ORDER_OUT_FOR_DELIVERY','ORDER_SHIPPED','PRODUCT_OUT_OF_STOCK','REFUND_COMPLETED','REFUND_PENDING','REPLACEMENT_REQUIRED','RETURN_APPROVED','RETURN_PARCEL_RECEIVED','RETURN_REJECTED','RETURN_REQUEST_SUBMITTED','RETURN_SHIP_REMINDER','SELLER_ACTIVATED','SELLER_APPROVED','SELLER_REGISTRATION_PENDING','SELLER_REJECTED','SELLER_SUSPENDED') NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_notification_user` (`user_id`),
  KEY `idx_notification_read` (`read_flag`),
  CONSTRAINT `FK9y21adhxn0ayjhfocscqox7bh` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `order_items`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `price` decimal(12,2) NOT NULL,
  `product_image_url` varchar(500) DEFAULT NULL,
  `product_name` varchar(255) DEFAULT NULL,
  `qty` int NOT NULL,
  `order_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKbioxgbv59vetrxe0ejfubep1w` (`order_id`),
  KEY `FKocimc7dtr037rh4ls4l95nlfi` (`product_id`),
  CONSTRAINT `FKbioxgbv59vetrxe0ejfubep1w` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`),
  CONSTRAINT `FKocimc7dtr037rh4ls4l95nlfi` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `order_seller_acknowledgements`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_seller_acknowledgements` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `confirmed_at` datetime(6) NOT NULL,
  `order_id` bigint NOT NULL,
  `seller_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_order_seller_ack` (`order_id`,`seller_id`),
  KEY `FKj0esm8sysnwjodi6cbcoor1fw` (`seller_id`),
  CONSTRAINT `FKct8x5dhc46vknrx9m4tmjmv0o` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`),
  CONSTRAINT `FKj0esm8sysnwjodi6cbcoor1fw` FOREIGN KEY (`seller_id`) REFERENCES `sellers` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `orders`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `delivered_at` datetime(6) DEFAULT NULL,
  `order_notes` varchar(500) DEFAULT NULL,
  `order_number` varchar(50) NOT NULL,
  `payment_status` enum('CANCELLED','COMPLETED','FAILED','PENDING','REFUNDED') NOT NULL,
  `shipping_charge` decimal(12,2) DEFAULT NULL,
  `status` enum('CANCELLED','CONFIRMED','DELIVERED','OUT_FOR_DELIVERY','PENDING','PROCESSING','RETURNED','SHIPPED') NOT NULL,
  `status_note` varchar(500) DEFAULT NULL,
  `subtotal` decimal(12,2) NOT NULL,
  `total` decimal(12,2) NOT NULL,
  `tracking_number` varchar(100) DEFAULT NULL,
  `address_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_order_number` (`order_number`),
  KEY `idx_order_user` (`user_id`),
  KEY `idx_order_status` (`status`),
  KEY `FKhlglkvf5i60dv6dn397ethgpt` (`address_id`),
  CONSTRAINT `FK32ql8ubntj5uh44ph9659tiih` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKhlglkvf5i60dv6dn397ethgpt` FOREIGN KEY (`address_id`) REFERENCES `addresses` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `otp_verifications`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `otp_verifications` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `email` varchar(150) NOT NULL,
  `expires_at` datetime(6) NOT NULL,
  `failed_attempts` int NOT NULL,
  `otp` varchar(100) NOT NULL,
  `purpose` enum('FORGOT_PASSWORD','LOGIN','REGISTRATION') NOT NULL,
  `verified` bit(1) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_otp_email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `payments`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `amount` decimal(12,2) NOT NULL,
  `method` enum('COD','RAZORPAY') NOT NULL,
  `raw_response` text,
  `razorpay_order_id` varchar(100) DEFAULT NULL,
  `status` enum('CANCELLED','COMPLETED','FAILED','PENDING','REFUNDED') NOT NULL,
  `transaction_id` varchar(100) DEFAULT NULL,
  `order_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK8vo36cen604as7etdfwmyjsxt` (`order_id`),
  KEY `idx_payment_transaction` (`transaction_id`),
  KEY `idx_payment_razorpay_order` (`razorpay_order_id`),
  CONSTRAINT `FK81gagumt0r8y3rmudcgpbk42l` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `pending_checkout_items`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pending_checkout_items` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `price` decimal(12,2) NOT NULL,
  `product_image_url` varchar(500) DEFAULT NULL,
  `product_name` varchar(255) NOT NULL,
  `qty` int NOT NULL,
  `pending_checkout_id` bigint NOT NULL,
  `product_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKquwqeu9wrt7ydmcs54bp2p8i1` (`pending_checkout_id`),
  KEY `FKfn7j35iut1tpufr1ej7widp17` (`product_id`),
  CONSTRAINT `FKfn7j35iut1tpufr1ej7widp17` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`),
  CONSTRAINT `FKquwqeu9wrt7ydmcs54bp2p8i1` FOREIGN KEY (`pending_checkout_id`) REFERENCES `pending_checkouts` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `pending_checkouts`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pending_checkouts` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `buy_now` bit(1) NOT NULL,
  `checkout_reference` varchar(40) NOT NULL,
  `expires_at` datetime(6) NOT NULL,
  `order_notes` varchar(500) DEFAULT NULL,
  `order_number` varchar(50) DEFAULT NULL,
  `razorpay_order_id` varchar(100) DEFAULT NULL,
  `shipping_charge` decimal(12,2) DEFAULT NULL,
  `status` enum('COMPLETED','EXPIRED','FAILED','PENDING') NOT NULL,
  `subtotal` decimal(12,2) NOT NULL,
  `total` decimal(12,2) NOT NULL,
  `address_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_pending_checkout_ref` (`checkout_reference`),
  UNIQUE KEY `idx_pending_checkout_razorpay` (`razorpay_order_id`),
  KEY `idx_pending_checkout_user` (`user_id`),
  KEY `FKtpgs4iy6yqrxtlaqgqxd0h2fk` (`address_id`),
  CONSTRAINT `FKq9gtq9lga5mw64llxyu80xmk8` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKtpgs4iy6yqrxtlaqgqxd0h2fk` FOREIGN KEY (`address_id`) REFERENCES `addresses` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `pending_registrations`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pending_registrations` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `email` varchar(150) NOT NULL,
  `expires_at` datetime(6) NOT NULL,
  `mobile` varchar(15) DEFAULT NULL,
  `name` varchar(100) NOT NULL,
  `password` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_pending_reg_email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `product_images`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_images` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `image_public_id` varchar(255) DEFAULT NULL,
  `image_url` varchar(500) NOT NULL,
  `is_primary` bit(1) NOT NULL,
  `sort_order` int DEFAULT NULL,
  `product_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKqnq71xsohugpqwf3c9gxmsuy` (`product_id`),
  CONSTRAINT `FKqnq71xsohugpqwf3c9gxmsuy` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `products`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `products` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `best_seller` bit(1) NOT NULL,
  `brand` varchar(100) DEFAULT NULL,
  `color` varchar(50) DEFAULT NULL,
  `description` text,
  `discount` decimal(5,2) DEFAULT NULL,
  `image360url` varchar(500) DEFAULT NULL,
  `latest_arrival` bit(1) NOT NULL,
  `name` varchar(255) NOT NULL,
  `occasion` varchar(100) DEFAULT NULL,
  `price` decimal(12,2) NOT NULL,
  `product_code` varchar(50) NOT NULL,
  `rating` decimal(3,2) DEFAULT NULL,
  `review_count` int DEFAULT NULL,
  `selling_price` decimal(12,2) NOT NULL,
  `slug` varchar(300) DEFAULT NULL,
  `status` enum('ACTIVE','DISCONTINUED','INACTIVE','OUT_OF_STOCK') NOT NULL,
  `stock` int NOT NULL,
  `video_url` varchar(500) DEFAULT NULL,
  `category_id` bigint DEFAULT NULL,
  `fabric_type_id` bigint DEFAULT NULL,
  `seller_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_product_code` (`product_code`),
  UNIQUE KEY `idx_product_slug` (`slug`),
  KEY `idx_product_category` (`category_id`),
  KEY `idx_product_status` (`status`),
  KEY `idx_product_seller` (`seller_id`),
  KEY `FKpbmacfcdmxnb4ey46gc2n8pvn` (`fabric_type_id`),
  CONSTRAINT `FKepbha8uixgrmnejm27n6e1kkd` FOREIGN KEY (`seller_id`) REFERENCES `sellers` (`id`),
  CONSTRAINT `FKog2rp4qthbtt2lfyhfo32lsw9` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`),
  CONSTRAINT `FKpbmacfcdmxnb4ey46gc2n8pvn` FOREIGN KEY (`fabric_type_id`) REFERENCES `fabric_types` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `refresh_tokens`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `refresh_tokens` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `expires_at` datetime(6) NOT NULL,
  `revoked` bit(1) NOT NULL,
  `token` varchar(500) NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_refresh_token` (`token`),
  KEY `FK1lih5y2npsf8u5o3vhdb9y0os` (`user_id`),
  CONSTRAINT `FK1lih5y2npsf8u5o3vhdb9y0os` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `refund_transactions`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `refund_transactions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `amount` decimal(12,2) NOT NULL,
  `raw_response` text,
  `razorpay_refund_id` varchar(100) DEFAULT NULL,
  `status` enum('COMPLETED','FAILED','PENDING') NOT NULL,
  `return_request_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_refund_transactions_return_request` (`return_request_id`),
  CONSTRAINT `FKl7e2cm6onufc6htkkclv6rna` FOREIGN KEY (`return_request_id`) REFERENCES `return_requests` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `return_history`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `return_history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `changed_by_role` enum('ROLE_ADMIN','ROLE_SELLER','ROLE_USER') DEFAULT NULL,
  `changed_by_user_id` bigint DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `from_status` enum('CUSTOMER_SHIPPED','EXCHANGE_COMPLETED','EXCHANGE_PROCESSING','EXCHANGE_SHIPPED','PARCEL_RECEIVED','QUALITY_CHECK','REFUND_COMPLETED','REFUND_INITIATED','RETURN_APPROVED','RETURN_REJECTED','RETURN_REQUESTED') DEFAULT NULL,
  `remarks` varchar(1000) DEFAULT NULL,
  `to_status` enum('CUSTOMER_SHIPPED','EXCHANGE_COMPLETED','EXCHANGE_PROCESSING','EXCHANGE_SHIPPED','PARCEL_RECEIVED','QUALITY_CHECK','REFUND_COMPLETED','REFUND_INITIATED','RETURN_APPROVED','RETURN_REJECTED','RETURN_REQUESTED') NOT NULL,
  `return_request_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKm83i9vf5qooetel7xuylp4hw4` (`return_request_id`),
  CONSTRAINT `FKm83i9vf5qooetel7xuylp4hw4` FOREIGN KEY (`return_request_id`) REFERENCES `return_requests` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `return_images`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `return_images` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `image_url` varchar(500) NOT NULL,
  `public_id` varchar(255) DEFAULT NULL,
  `sort_order` int NOT NULL,
  `return_request_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKkigv36kvtw3om1p7qo66bfis2` (`return_request_id`),
  CONSTRAINT `FKkigv36kvtw3om1p7qo66bfis2` FOREIGN KEY (`return_request_id`) REFERENCES `return_requests` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `return_requests`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `return_requests` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `admin_remarks` varchar(1000) DEFAULT NULL,
  `customer_remarks` varchar(1000) DEFAULT NULL,
  `delivered_at_snapshot` datetime(6) NOT NULL,
  `description` text,
  `exchange_tracking_number` varchar(100) DEFAULT NULL,
  `preferred_resolution` enum('EXCHANGE','REFUND') NOT NULL,
  `reason` enum('DAMAGED','NOT_AS_DESCRIBED','OTHER','QUALITY_ISSUE','SIZE_ISSUE','WRONG_ITEM') NOT NULL,
  `refund_amount` decimal(12,2) DEFAULT NULL,
  `rejection_reason` varchar(1000) DEFAULT NULL,
  `return_number` varchar(50) NOT NULL,
  `return_type` enum('EXCHANGE','RETURN') NOT NULL,
  `ship_reminder_sent` bit(1) NOT NULL,
  `status` enum('CUSTOMER_SHIPPED','EXCHANGE_COMPLETED','EXCHANGE_PROCESSING','EXCHANGE_SHIPPED','PARCEL_RECEIVED','QUALITY_CHECK','REFUND_COMPLETED','REFUND_INITIATED','RETURN_APPROVED','RETURN_REJECTED','RETURN_REQUESTED') NOT NULL,
  `customer_id` bigint NOT NULL,
  `order_id` bigint NOT NULL,
  `order_item_id` bigint NOT NULL,
  `seller_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_return_number` (`return_number`),
  UNIQUE KEY `idx_return_order_item` (`order_item_id`),
  KEY `idx_return_customer` (`customer_id`),
  KEY `idx_return_seller` (`seller_id`),
  KEY `idx_return_status` (`status`),
  KEY `FKbski88d6kewx0cbj5pk7nes01` (`order_id`),
  CONSTRAINT `FKbski88d6kewx0cbj5pk7nes01` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`),
  CONSTRAINT `FKj8p930ntqh3nxl7jtc1xoxwy9` FOREIGN KEY (`customer_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKqmtolfa50ie1jxfsak1e8jnkb` FOREIGN KEY (`order_item_id`) REFERENCES `order_items` (`id`),
  CONSTRAINT `FKrfch3nb3hh5hb6y6njtkbo1rb` FOREIGN KEY (`seller_id`) REFERENCES `sellers` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `return_shipments`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `return_shipments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `courier_company` enum('BLUE_DART','DELHIVERY','DTDC','INDIA_POST','OTHER') NOT NULL,
  `dispatch_date` date NOT NULL,
  `receipt_public_id` varchar(255) DEFAULT NULL,
  `receipt_url` varchar(500) DEFAULT NULL,
  `tracking_number` varchar(100) NOT NULL,
  `return_request_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKfx4s0x49ygbdelhp1yxphtvxm` (`return_request_id`),
  CONSTRAINT `FKr0e6nlw1swvk8ealjr3qc5hih` FOREIGN KEY (`return_request_id`) REFERENCES `return_requests` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `reviews`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reviews` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `approved` bit(1) NOT NULL,
  `comment` text,
  `photo_url` varchar(500) DEFAULT NULL,
  `rating` int NOT NULL,
  `product_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_review_product` (`product_id`),
  KEY `idx_review_user` (`user_id`),
  CONSTRAINT `FKcgy7qjc1r99dp117y9en6lxye` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKpl51cejpw4gy5swfar8br9ngi` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `seller_settlements`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `seller_settlements` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `commission_amount` decimal(12,2) NOT NULL,
  `commission_percentage` decimal(5,2) NOT NULL,
  `commission_rule_id` bigint DEFAULT NULL,
  `gross_amount` decimal(12,2) NOT NULL,
  `matched_category_name` varchar(150) DEFAULT NULL,
  `matched_fabric` varchar(100) DEFAULT NULL,
  `net_seller_amount` decimal(12,2) NOT NULL,
  `settlement_date` datetime(6) DEFAULT NULL,
  `settlement_status` enum('FAILED','PAID','PENDING','PROCESSING') NOT NULL,
  `transaction_reference` varchar(120) DEFAULT NULL,
  `order_id` bigint NOT NULL,
  `order_item_id` bigint NOT NULL,
  `seller_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_settlement_order_item` (`order_item_id`),
  KEY `idx_settlement_seller` (`seller_id`),
  KEY `idx_settlement_order` (`order_id`),
  KEY `idx_settlement_status` (`settlement_status`),
  CONSTRAINT `FKmkjiuimtrbfh0dd2j3h587yxd` FOREIGN KEY (`order_item_id`) REFERENCES `order_items` (`id`),
  CONSTRAINT `FKqfniajeerjbgt83nb2n9n55pj` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`),
  CONSTRAINT `FKtflox30dn80ux67j20ibres8p` FOREIGN KEY (`seller_id`) REFERENCES `sellers` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sellers`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sellers` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `bank_account_holder` varchar(120) NOT NULL,
  `bank_account_number` varchar(30) NOT NULL,
  `bank_ifsc` varchar(15) NOT NULL,
  `bank_name` varchar(120) NOT NULL,
  `business_address` varchar(500) NOT NULL,
  `business_logo_public_id` varchar(255) DEFAULT NULL,
  `business_logo_url` varchar(500) DEFAULT NULL,
  `business_name` varchar(150) NOT NULL,
  `city` varchar(100) NOT NULL,
  `email` varchar(150) NOT NULL,
  `gst` varchar(20) DEFAULT NULL,
  `mobile` varchar(15) NOT NULL,
  `owner_name` varchar(100) NOT NULL,
  `pan` varchar(15) DEFAULT NULL,
  `pincode` varchar(10) NOT NULL,
  `platform_owned` bit(1) NOT NULL,
  `profile_image_public_id` varchar(255) DEFAULT NULL,
  `profile_image_url` varchar(500) DEFAULT NULL,
  `rejection_reason` varchar(500) DEFAULT NULL,
  `state` varchar(100) NOT NULL,
  `status` enum('ACTIVE','APPROVED','INACTIVE','PENDING','REJECTED','SUSPENDED') NOT NULL,
  `upi_id` varchar(100) DEFAULT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_seller_email` (`email`),
  UNIQUE KEY `idx_seller_user` (`user_id`),
  KEY `idx_seller_status` (`status`),
  CONSTRAINT `FKjnqi0k1rlkb8h3fus7f5wfqd1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `shipping_rules`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `shipping_rules` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `active` bit(1) NOT NULL,
  `city` varchar(100) DEFAULT NULL,
  `end_date` datetime(6) DEFAULT NULL,
  `is_first_order_only` bit(1) NOT NULL,
  `minimum_order_amount` decimal(12,2) DEFAULT NULL,
  `pincode` varchar(10) DEFAULT NULL,
  `priority` int NOT NULL,
  `rule_name` varchar(150) NOT NULL,
  `rule_type` enum('CITY_WISE','DEFAULT_CHARGE','EXPRESS_DELIVERY','FIRST_ORDER_FREE','MINIMUM_ORDER_FREE','PINCODE_WISE','PROMOTIONAL_FREE','STATE_WISE') NOT NULL,
  `shipping_charge` decimal(12,2) NOT NULL,
  `start_date` datetime(6) DEFAULT NULL,
  `state` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_shipping_rule_type` (`rule_type`),
  KEY `idx_shipping_rule_active` (`active`),
  KEY `idx_shipping_rule_priority` (`priority`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `users`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `email` varchar(150) NOT NULL,
  `mobile` varchar(15) DEFAULT NULL,
  `name` varchar(100) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` enum('ROLE_ADMIN','ROLE_SELLER','ROLE_USER') NOT NULL,
  `status` enum('ACTIVE','BLOCKED','INACTIVE','PENDING_VERIFICATION') NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_user_email` (`email`),
  KEY `idx_user_mobile` (`mobile`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `wishlists`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wishlists` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `product_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_wishlist_user_product` (`user_id`,`product_id`),
  KEY `FKl7ao98u2bm8nijc1rv4jobcrx` (`product_id`),
  CONSTRAINT `FK330pyw2el06fn5g28ypyljt16` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKl7ao98u2bm8nijc1rv4jobcrx` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-30 17:03:26
