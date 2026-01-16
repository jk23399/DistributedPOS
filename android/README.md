# SimplePOS - Restaurant Point of Sale System

A fully functional Android POS application built from scratch to solve real-world restaurant management challenges. Designed for tablets with an intuitive touch interface and offline-first architecture.

## Project Overview

SimplePOS is a complete restaurant management solution that handles order taking and modification, kitchen communication, payment processing with tax/tip calculations, receipt printing to thermal printers, order history and analytics, and floor plan management.

Built for small to medium restaurants looking for an affordable, reliable POS system without monthly subscription fees.

## Why I Built This

Traditional POS systems are expensive ($50-200/month) and require internet connectivity. I wanted to create a free, offline-capable alternative that stores all data locally while maintaining professional features like network printing and order history.

## Technical Stack

- Language: Kotlin
- UI: Jetpack Compose (100% declarative UI)
- Architecture: MVVM with Repository pattern
- Database: Room (SQLite) with Flow-based reactive queries
- State Management: StateFlow and Compose State
- Navigation: Jetpack Navigation Compose
- Async: Coroutines with Dispatchers
- Printing: Raw socket communication with ESC/POS protocol

## Key Features & Technical Highlights

### Real-time Order Management
Challenge: Track order state changes (new items vs. already ordered) while maintaining data consistency

Solution: Implemented a dual-list system with cartItems (new) and orderedItems (sent to kitchen), using Room's Flow for automatic UI updates. Visual feedback with color-coded items (green = new) and bold text for easy identification.

### Complex Financial Calculations
Challenge: Apply discount before tax, then add gratuity on discounted subtotal

Implementation: Reactive calculation chain using combine() operator with multiple StateFlows. Formula: (Subtotal - Discount) * Tax + (Subtotal - Discount) * Gratuity = Total

### Dynamic Floor Plan Editor
Challenge: Allow drag-and-drop table positioning with persistence

Solution: Custom gesture detection with detectTransformGestures for pan/zoom. Temporary state during editing, batch save to database on confirmation.

### Network Thermal Printing
Challenge: Print receipts to 80mm thermal printers over WiFi without external libraries

Implementation: Raw TCP socket connection (port 9100), ESC/POS command generation from receipt data, async printing with proper error handling, and configurable IP/port via SharedPreferences.

### Order History with Date Filtering
Challenge: Efficiently query 3 years of orders without performance issues

Solution: Indexed timestamp columns in Room, dynamic query generation based on date range, and lazy loading with LazyColumn for smooth scrolling.

### Offline-First Architecture
All data stored locally in SQLite. No network dependency for core operations. Only network printer requires connectivity. Handles concurrent order modifications with Room transactions.

## Technical Decisions

Why Compose over XML?
Faster development with declarative syntax, type-safe navigation, better state management with remember/derivedStateOf, and it's the modern Android development standard.

Why Room over raw SQLite?
Compile-time SQL verification, automatic Flow-based updates, less boilerplate code, and migration support.

Why local storage over cloud?
No monthly server costs, works without internet, faster response times, and data privacy for small businesses.

## What I Learned

- Complex State Management: Managing multiple interconnected StateFlows and handling race conditions
- Low-level Protocols: Implementing ESC/POS printer communication from scratch
- UI/UX Design: Creating an intuitive interface for non-technical users (restaurant staff)
- Database Optimization: Designing efficient schemas and queries for thousands of orders
- Error Handling: Graceful degradation when printer fails or network issues occur

## Challenges Overcome

- Merging orders with same items: Preventing duplicate orders when incrementing quantities
- Canceled item tracking: Maintaining audit trail while allowing order modifications
- Date range queries: Optimizing database queries for large date ranges
- Printer compatibility: Supporting different thermal printer models with ESC/POS standard
- Orientation handling: Managing state across screen rotations with SavedStateHandle

## Future Enhancements

- Export to CSV for accounting integration
- Multi-language support
- Split payment functionality
- Server sync for multi-device setups
- Analytics dashboard with sales reports

## Project Structure
```
app/src/main/java/com/example/simplepos/
├── data/                    # Database entities, DAOs, repositories
├── ui/
│   ├── pos/                # Order management screens
│   ├── menu/               # Settings and menu management
│   ├── tables/             # Table selection and floor plan
│   ├── receipt/            # Receipt formatting and printing
│   └── theme/              # Material3 theme customization
└── PosApplication.kt       # App-level state and configuration
```

## Setup & Installation

1. Clone the repository
2. Open in Android Studio Ladybug or later
3. Build and run on Android 8.0+ device or emulator
4. Configure business profile and printer settings in app

## Project Stats

- Lines of Code: ~5,000
- Screens: 12 main screens
- Database Tables: 4 (Orders, OrderItems, MenuItems, Tables)
- Development Time: 3 weeks
- Target API: Android 26-35

## License

MIT License - Free for personal and commercial use
