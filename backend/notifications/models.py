from django.db import models
from accounts.models import User

class Notification(models.Model):
    title = models.CharField(max_length=200)
    content = models.TextField(null=True, blank=True) # (kept for legacy support)
    sender = models.ForeignKey(User, on_delete=models.CASCADE, related_name='sent_notifications')
    target_role = models.CharField(max_length=20, default='ALL') # STUDENT, FACULTY, ADMIN, ALL (kept for legacy support)
    is_active = models.BooleanField(default=True)
    created_at = models.DateTimeField(auto_now_add=True)
    
    # New required fields
    message = models.TextField(null=True, blank=True)
    notification_type = models.CharField(max_length=50, null=True, blank=True)

    def save(self, *args, **kwargs):
        if self.message and not self.content:
            self.content = self.message
        elif self.content and not self.message:
            self.message = self.content
        super().save(*args, **kwargs)

    def __str__(self):
        return f"{self.title} ({self.notification_type or 'General'})"
