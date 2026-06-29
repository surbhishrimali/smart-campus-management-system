from rest_framework import serializers
from notifications.models import Notification

class NotificationSerializer(serializers.ModelSerializer):
    sender_name = serializers.EmailField(source='sender.email', read_only=True)

    class Meta:
        model = Notification
        fields = [
            'id', 'title', 'content', 'sender', 'sender_name', 'target_role', 'is_active', 'created_at',
            'message', 'notification_type'
        ]
        read_only_fields = ['sender']
