# Sathi AI Android Architecture

## Execution Pipeline
USER VOICE -> VOICE CAPTURE -> SPEECH TO TEXT -> INTENT CLASSIFIER -> ACTION ROUTER -> REAL ACTION EXECUTOR -> RESULT VERIFICATION -> RESPONSE -> TEXT TO SPEECH

## Tri-Mode Separation Rules
1. CONVERSATION: Handled by local/remote conversational logic. Never auto-routes to search.
2. ACTION: Direct Android OS execution. Must return structured `ActionResult`.
3. INFORMATION: Explicit web search and real-time data lookup only when requested.
