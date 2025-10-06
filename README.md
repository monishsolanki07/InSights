# Insight News Reels App

Modern Android news app delivering curated news articles in a visually engaging “Reels” style vertical feed. Features include offline caching, efficient pagination, bookmarking, keyword search, and anchor speaking articles with Text-to-Speech (TTS).

---

## Monorepo / Modular Structure

- `app/` — Android app codebase  
  - `ui/`  
    - `reels/` — News reels UI components and `ReelsViewModel`  
    - `bookmarks/` — Bookmark screen and ViewModel  
    - `search/` — Home screen search UI and ViewModel  
    - `speech/` — TTS UI controls and helper classes  
    - `common/` — Shared UI components (loading, error states)  
  - `data/`  
    - `model/` — Data classes e.g. `NewArticle.kt`  
    - `remote/` — API networking using Retrofit and serialization  
    - `repository/` — Data repositories handling caching and fetching  
    - `local/` — DataStore and local database entities  
  - `util/` — Utility classes and helpers (e.g., TTS helpers, extensions)

---

## Default Ports and Config

- **News API Backend** — Configured in repository and network layer, typically using public or custom API URL.
- **Local Data Storage** — Android DataStore (no ports; local).
  
---

## Quick Start (Development)

1. Clone the repo:

git clone https://github.com/monishsolanki07/InSights.git
cd insight-news-reels/app


2. Configure your News API key in the repository or application `build.gradle` or secure config file as described.

3. Open the project in Android Studio Arctic Fox or newer.

4. Run the app on an emulator or device.

---

## Features Overview

- **Reels-Style News Feed**  
  Swipe vertically through cards with large images, titles, and summaries.

- **Offline Cache with DataStore**  
  Articles cached locally for fast load & offline availability.

- **Paging 3 Pagination**  
  Efficient API paging for infinite scrolling.

- **Keyword Search on Home Screen**  
  Input keyword to fetch related news on demand.

- **Bookmarks**  
  Save and manage favorite articles.

- **Anchor Speaking Articles (TTS)**  
  Listen to article content via integrated Android Text-to-Speech for accessible, hands-free news consumption.

- **Smooth Image Loading and Prefetching**  
  Coil handles image caching and preloading ahead of user scroll.

- **Detailed Article View with Bottom Sheet**  
  Full article content and source link accessible in modal sheet.

- **Robust Error and Loading State Handling**  
  User-friendly UI for network/loading issues with retry options.

---

## Environment Variables / Configuration

- `NewsApiKey` — Your API key for the news service, set in your secure config or Gradle properties.
- `CacheExpiration` — Configurable cache expiry time in milliseconds.
- `PagingPageSize` — Number of articles fetched per API call (default 10).
- `SupportedCountries` — Country codes filter for news (e.g. `us,au`).
- `Languages` — Supported article languages (e.g. `en`).

---

## Services and Components

| Module/Package                 | Responsibility                           |
|-------------------------------|------------------------------------------|
| `data.model`                  | Data classes/models like `NewArticle`  |
| `data.remote`                 | Retrofit API interfaces and network calls  |
| `data.repository`             | Data fetching, caching, and repository pattern |
| `data.local`                  | DataStore caching and local entities    |
| `ui.reels`                   | News reels UI and ViewModel              |
| `ui.bookmarks`               | Bookmark UI and ViewModel                 |
| `ui.search`                  | Search screen UI and ViewModel            |
| `ui.speech`                  | Text-to-Speech UI components and logic    |
| `ui.common`                  | Shared loading spinners, error messages  |
| `di`                        | Hilt modules for Dependency Injection     |
| `util`                      | Helpers (extensions, TTS handler)         |

---

## Development Notes

- Uses **Paging 3** for scalable infinite scroll and load states integration.
- **DataStore** caches articles locally, minimizing network loads.
- The **search UI** allows keyword-based fetching of news in real-time.
- Text-to-Speech integration provides an **anchor speaking article** feature accessible via UI controls in each article card.
- Efficient **image prefetching** balances performance and memory usage.
- Strict **unidirectional data flow** ensures robust, maintainable state management.
- Error states and loading indicators provide immediate user feedback.
- Modular architecture with **Hilt** for DI improves testability and separation of concerns.

---

## Running the App

- Open Android Studio.
- Ensure API key is configured.
- Run on emulator or physical device.
- Use the home screen search bar to fetch news by keyword.
- Swipe vertically to browse reels.
- Tap bookmark icon to save articles.
- Tap “Speak” button on any article to listen to the content.
- Tap “Read More” for full article details in a bottom sheet.

---

## Future Enhancements

- User authentication and personalized feeds.
- Real-time collaborative annotations.
- Dark mode and accessibility improvements.
- Enhanced search with filters and autocomplete.
- Cross-device bookmark syncing with cloud backup.
- Analytics and usage tracking.

---

## License

MIT License — see [LICENSE](LICENSE) file for details.

---

## Contact

Created and maintained by Monish. For contributions or questions, open an issue or pull request on the repository.

