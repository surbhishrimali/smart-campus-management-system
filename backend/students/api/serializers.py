from rest_framework import serializers
from students.models import StudentProfile

class StudentProfileSerializer(serializers.ModelSerializer):
    email = serializers.EmailField(source='user.email', read_only=True)
    full_name = serializers.CharField(source='user.full_name', read_only=True)
    department = serializers.CharField(source='user.department', read_only=True)

    class Meta:
        model = StudentProfile
        fields = ['id', 'user', 'email', 'full_name', 'department', 'enrollment_number', 'branch', 'semester', 'section', 'phone', 'gpa']
