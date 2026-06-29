from rest_framework import serializers
from timetable.models import Timetable

class TimetableSerializer(serializers.ModelSerializer):
    faculty_name = serializers.CharField(source='faculty.full_name', read_only=True)

    class Meta:
        model = Timetable
        fields = ['id', 'semester', 'day', 'subject', 'faculty', 'faculty_name', 'room_no', 'start_time', 'end_time']
