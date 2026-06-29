from rest_framework import serializers
from attendance.models import Attendance

class AttendanceSerializer(serializers.ModelSerializer):
    student_email = serializers.EmailField(source='student.email', read_only=True)
    student_name = serializers.CharField(source='student.full_name', read_only=True)
    faculty_name = serializers.CharField(source='faculty.full_name', read_only=True)
    status = serializers.CharField(required=False, allow_null=True, allow_blank=True)

    class Meta:
        model = Attendance
        fields = ['id', 'student', 'student_email', 'student_name', 'faculty', 'faculty_name', 'subject', 'date', 'status', 'remarks', 'student_class', 'is_present']
        read_only_fields = ['faculty']

    def validate_status(self, value):
        if not value:
            return value
        val_lower = value.lower()
        if val_lower == 'present':
            return 'Present'
        elif val_lower == 'absent':
            return 'Absent'
        elif val_lower == 'leave':
            return 'Leave'
        raise serializers.ValidationError(f"'{value}' is not a valid choice. Choose from 'Present', 'Absent', or 'Leave'.")

