# API Keys Configuration

To set up your API keys:

1. Create a `local.properties` file in the project root (this file is gitignored)

2. Add your Gemini API key to the file:
   ```
   GEMINI_API_KEY=your_actual_api_key_here
   ```

3. The secrets-gradle-plugin will automatically make this available as `BuildConfig.GEMINI_API_KEY`

Note: Never commit API keys to version control!