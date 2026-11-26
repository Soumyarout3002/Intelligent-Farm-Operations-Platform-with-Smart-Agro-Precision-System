# Brevo SMTP setup for invitation emails

The application uses Brevo SMTP.

Required environment variables:
- MAIL_ENABLED=true enables email sending.
- MAIL_USERNAME is your Brevo SMTP login.
- MAIL_PASSWORD is your Brevo SMTP key.
- MAIL_FROM is your verified sender email.
- MAIL_DIAGNOSTICS=true enables SMTP diagnostics.
- FRONTEND_BASE_URL is the frontend URL.
- DB_PASSWORD is your local MySQL password.

SMTP host: smtp-relay.brevo.com
SMTP port: 587
Authentication: enabled
STARTTLS: enabled

Keep real credentials private. Never commit .env files or SMTP keys.
