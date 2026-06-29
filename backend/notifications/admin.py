from django.contrib import admin
from notifications.models import Notification

@admin.register(Notification)
class NotificationAdmin(admin.ModelAdmin):
    list_display = ('title', 'sender', 'notification_type', 'target_role', 'is_active', 'created_at')
    list_filter = ('notification_type', 'target_role', 'is_active', 'created_at')
    search_fields = ('title', 'message', 'content', 'sender__email')

