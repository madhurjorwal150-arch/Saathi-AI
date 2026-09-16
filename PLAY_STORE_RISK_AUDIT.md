# Play Store Policy & Risk Audit

## Sensitive Permissions Declared
1. `QUERY_ALL_PACKAGES`: High Play Store risk. Requires declared Assistant/Device Search exception in Google Play Console.
2. `RECORD_AUDIO` & `FOREGROUND_SERVICE_MICROPHONE`: Requires clear runtime disclosure and persistent notification.
3. `SYSTEM_ALERT_WINDOW`: High risk if requested unnecessarily. Retain only if floating orb overlay is active.
