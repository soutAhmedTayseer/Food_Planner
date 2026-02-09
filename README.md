# 🍲 Food Planner Android App

![Language](https://img.shields.io/badge/Language-Java-orange.svg)
![Platform](https://img.shields.io/badge/Platform-Android-green.svg)
![Architecture](https://img.shields.io/badge/Architecture-MVP-blue.svg)
![Database](https://img.shields.io/badge/Database-Room%20%7C%20Firestore-red.svg)
![Status](https://img.shields.io/badge/Status-Completed-success.svg)

> **Cook. Eat. Repeat.** > A comprehensive Android application to discover recipes, plan weekly meals, and manage your culinary journey.

---

## 📸 App Gallery

<details>
  <summary><strong>👇 Click to expand App Screenshots</strong></summary>
  <br>
  <table align="center">
    <tr>
      <td align="center">
        <img src="screenshots/splash.png" width="200" alt="Splash Screen" /><br>
        <sub><b>Splash Screen</b></sub>
      </td>
      <td align="center">
        <img src="screenshots/login.png" width="200" alt="Login" /><br>
        <sub><b>Login</b></sub>
      </td>
      <td align="center">
        <img src="screenshots/signup.png" width="200" alt="Sign Up" /><br>
        <sub><b>Sign Up</b></sub>
      </td>
      <td align="center">
        <img src="screenshots/home.png" width="200" alt="Home" /><br>
        <sub><b>Home Inspiration</b></sub>
      </td>
    </tr>
    <tr>
      <td align="center">
        <img src="screenshots/daily_meal.png" width="200" alt="Daily Meal" /><br>
        <sub><b>Daily Flip Card</b></sub>
      </td>
      <td align="center">
        <img src="screenshots/search.png" width="200" alt="Search" /><br>
        <sub><b>Search & Filters</b></sub>
      </td>
      <td align="center">
        <img src="screenshots/details.png" width="200" alt="Details" /><br>
        <sub><b>Meal Details</b></sub>
      </td>
      <td align="center">
        <img src="screenshots/details_scroll.png" width="200" alt="Instructions" /><br>
        <sub><b>Instructions</b></sub>
      </td>
    </tr>
    <tr>
      <td align="center">
        <img src="screenshots/calendar.png" width="200" alt="Plan" /><br>
        <sub><b>Weekly Planner</b></sub>
      </td>
      <td align="center">
        <img src="screenshots/favorites.png" width="200" alt="Favorites" /><br>
        <sub><b>Favorites List</b></sub>
      </td>
      <td align="center">
        <img src="screenshots/profile.png" width="200" alt="Profile" /><br>
        <sub><b>User Profile</b></sub>
      </td>
      <td align="center">
        <img src="screenshots/guest.png" width="200" alt="Guest" /><br>
        <sub><b>Guest Mode</b></sub>
      </td>
    </tr>
  </table>
</details>

---

## 📱 Project Overview

**Food Planner** is a native Android application designed to simplify meal management. It integrates cloud-based authentication, remote API data fetching, and local offline caching to provide a seamless user experience.

Whether you are a home cook looking for inspiration or a meal prepper organizing your week, Food Planner helps you:
* **Discover** new recipes from around the world.
* **Plan** your week with a dedicated calendar.
* **Save** your favorite meals for offline access.

---

## ✨ Key Features

### 🔐 Authentication & Profile
* **Multi-Method Login:** Sign in via Email/Password or **Google Sign-In** (Firebase).
* **Guest Mode:** Explore the app with restricted access before committing to an account.
* **Data Sync:** User data is synchronized between **Firestore** (Cloud) and **Room** (Local).

### 🏠 Home & Discovery
* **Daily Inspiration:** A "Meal of the Day" featuring a **Flip Card Animation** to reveal details.
* **Carousel:** A horizontally scrolling list of random meals fetched via **Parallel Network Requests** (RxJava) for instant loading.
* **Connectivity:** Smart handling of network states with **Shimmer Loading Effects**.

### 🔍 Search & Filtering
* **Triple Filter System:** Search by **Category** (e.g., Seafood), **Area** (e.g., Italian), or **Ingredient** (e.g., Chicken).
* **Smart Search:** Real-time **Debounced Search** (300ms) filters results locally to save data and battery.
* **Visuals:** **Shared Element Transitions** animate food images smoothly from grid to details.

### 📅 Meal Planning (Core)
* **Weekly Scheduler:** Assign specific meals to specific dates using an interactive Calendar.
* **Conflict Handling:** Prevents duplicate meals on the same day.
* **Guest Restriction:** Prompts guests to log in to use planning features.

### ❤️ Favorites
* **Offline Access:** All favorites are cached locally in **Room Database**.
* **Reactive Updates:** Lists update automatically using **RxJava Flowables**.

---

## 🛠️ Technical Architecture

The application follows the **Model-View-Presenter (MVP)** architectural pattern to ensure separation of concerns and testability.

| Layer | Responsibility | Components |
| :--- | :--- | :--- |
| **View** | Displays data & captures user input. Passive interface. | `Activities`, `Fragments`, `XML` |
| **Presenter** | The "Brain". Handles logic, data retrieval decisions, and UI commands. | `HomePresenter`, `SearchPresenter` |
| **Model** | Single Source of Truth for data (Network vs Local). | `Repository`, `RemoteDataSource`, `LocalDataSource` |



### Navigation
* **Single-Activity Architecture:** Uses `HomeActivity` as the container.
* **Jetpack Navigation:** Manages fragment transactions and `SafeArgs` for data passing.

---

## 🏗️ Tech Stack & Libraries

* **Language:** Java 11
* **Networking:** [Retrofit 2](https://square.github.io/retrofit/) + Gson
* **Async Logic:** [RxJava 3](https://github.com/ReactiveX/RxJava) + RxAndroid
* **Database:** [Room Persistence Library](https://developer.android.com/training/data-storage/room)
* **Cloud Backend:** [Firebase](https://firebase.google.com/) (Auth, Firestore)
* **Image Loading:** [Glide](https://github.com/bumptech/glide)
* **Video:** [Android-YouTube-Player](https://github.com/PierfrancescoSoffritti/android-youtube-player)
* **Animations:** [Lottie](https://airbnb.io/lottie/#/) (JSON Vector Animations)
* **Design:** Material Design 3 (Material You)

---

## 💾 Data Flow

### 1. Network Layer (TheMealDB API)
* Base URL: `https://www.themealdb.com/api/json/v1/1/`
* **Optimization:** Uses a Custom `MealDeserializer` to parse the API's non-standard ingredient list format into a clean Java List.

### 2. Local Database (Room)
* **`fav_meals` table:** Stores favorite recipes.
* **`plan_meals` table:** Links meals to specific dates (`Composite Key: mealId + date + userId`).
* **`user_table`:** Caches user session for offline capability.

---

## 🎨 UI/UX Design System

The app features a **"Warm & Organic"** theme designed to stimulate appetite.

* **Primary Color:** Deep Orange (`#E65100`)
* **Background:** Cream (`#FFF3E0`)
* **Typography:** *Playfair Display* (Headlines) & *Lato* (Body)
* **Components:**
    * **Floating Search Bar:** Material Card style.
    * **Parallax Header:** Collapsing toolbar in Meal Details.
    * **Skeleton Loading:** Shimmer effects for perceived performance.

---

## 🚀 Setup & Installation

1.  **Clone the Repo**
    ```bash
    git clone [https://github.com/YourUsername/FoodPlanner.git](https://github.com/YourUsername/FoodPlanner.git)
    ```
2.  **Open in Android Studio**
    * Requires Android Studio Iguana or newer.
    * JDK 11 required.
3.  **Firebase Setup**
    * Create a project in the [Firebase Console](https://console.firebase.google.com/).
    * Enable **Authentication** (Email/Password & Google).
    * Enable **Firestore Database**.
    * Download `google-services.json` and place it in the `app/` directory.
4.  **Build & Run**
    * Sync Gradle files.
    * Run on an Emulator (Pixel 6 recommended) or physical device.

---

## 🔮 Future Improvements

* [ ] **Push Notifications:** Reminders for planned meals.
* [ ] **Unit Tests:** Expand coverage for Presenters.

---

<div align="center">
  <sub>Built with ❤️ by Ahmed Tayseer</sub>
</div>
