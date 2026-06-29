from rest_framework import serializers
from complaints.models import Complaint
from accounts.models import User

class ComplaintSerializer(serializers.ModelSerializer):
    user_email = serializers.EmailField(source='user.email', read_only=True)
    resolved_by_name = serializers.CharField(source='resolved_by.full_name', read_only=True)
    user = serializers.PrimaryKeyRelatedField(
        queryset=User.objects.all(),
        required=False
    )

    class Meta:
        model = Complaint
        fields = [
            'id', 'user', 'user_email', 'title', 'description', 
            'status', 'priority', 'admin_reply', 'resolved_by', 
            'resolved_by_name', 'resolved_at', 'created_at'
        ]
