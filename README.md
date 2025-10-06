# Insight News Reels App

Insight is a modern Android news application delivering a visually engaging “Reels” style vertical news feed. It combines efficient network pagination, offline caching, bookmarking, keyword search, and an innovative **anchor speaking article** feature via Text-to-Speech (TTS) for an immersive audio-visual experience.

---

## Key Features

- **Reels-Style Vertical Feed:**  
  Swipe seamlessly through news articles displayed with large images and concise summaries, optimized for quick browsing.

- **Offline Caching with DataStore:**  
  Articles are cached locally with automatic expiry, ensuring quick load times and offline access.

- **Efficient Pagination with Paging 3:**  
  Infinite scrolling with optimized network requests.

- **Keyword Search on Home Screen:**  
  Search for news by keywords, fetching related articles in real-time.

- **Bookmarking:**  
  Save favorite articles for easy retrieval later.

- **Image Optimization with Coil:**  
  Prefetch images for smooth, lag-free scrolling experience.

- **Article Details with Bottom Sheet:**  
  Tap “Read More” to view full content, source links, and images.

- **Anchor Speaking Article (TTS):**  
  Tap "Speak" button on an article to listen to the content utilizing Android's Text-to-Speech engine, providing hands-free news consumption.

- **Robust Error & Loading States:**  
  Displays appropriate UI prompts during network fetches, errors, or when no articles are available.

---

## Architecture & Technologies

- **Jetpack Compose:** Modern, declarative UI toolkit.
- **MVVM Pattern:** Clear separation of UI logic and data handling.
- **Paging 3:** For scalable, efficient pagination.
- **DataStore:** Local caching for offline support.
- **Retrofit & Kotlin Serialization:** Network API calls and JSON parsing.
- **Coil:** Image loading and prefetching.
- **Hilt:** Dependency injection framework.
- **Kotlin Coroutines & Flow:** For asynchronous data streams.
- **Android Text-to-Speech (TTS):** For reading articles aloud.

---

## Project Structure

com.monish.insight
├─ data
│ ├─ model # Data classes: Article, API responses
│ ├─ remote # Retrofit API setup
│ ├─ repository # Data repositories for fetching and caching
│ └─ local # DataStore setup and local entities
├─ ui
│ ├─ reels # Reels UI & ViewModel
│ ├─ bookmarks # Bookmarks screen & ViewModel
│ ├─ search # Search UI components
│ ├─ speech # TTS implementation and toggle UI
│ └─ common # Shared components (loaders, errors)
├─ di # Dependency Injection modules (Hilt)
└─ util # Helpers, extensions, and TTS utilities


---

## Getting Started

1. **Clone the Repository:**

  git clone https://github.com/monishsolanki07/InSights.git


2. **Open in Android Studio:**  
   Use Android Studio Arctic Fox or newer.

3. **Configure API Key:**  
   Insert your News API key as instructed in the project setup.

4. **Build & Run:**  
   Deploy on a device or emulator.

5. **Explore Features:**
   - Scroll through engaging news reels
   - Use the search bar on the home screen for keyword-based news retrieval
   - Bookmark articles
   - Tap "Speak" to listen to the article aloud

---

## Developer Notes

- Efficiently manages pagination with Paging 3.
- Balances caching with DataStore for quick offline access.
- TTS enhances accessibility; utilize pause, resume, and stop controls.
- Modular architecture simplifies maintenance and future features.
- Error handling and UI states improve user experience.

---

## Future Enhancements

- User profile and personalized feeds
- Dark mode and accessibility improvements
- Search suggestions, filters
- Sync bookmarks across devices
- Analytics and user engagement tracking

---

## License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

---

## Contact

Created by Monish.  
For questions or contributions, please submit an issue or pull request.

