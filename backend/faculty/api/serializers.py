from rest_framework import serializers
from faculty.models import FacultyProfile

class FacultyProfileSerializer(serializers.ModelSerializer):
    email = serializers.EmailField(source='user.email', read_only=True)
    full_name = serializers.CharField(source='user.full_name', read_only=True)

    class Meta:
        model = FacultyProfile
        fields = ['id', 'user', 'email', 'full_name', 'faculty_id', 'department', 'designation', 'phone', 'specialization']
