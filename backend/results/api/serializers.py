from rest_framework import serializers
from results.models import Result

class ResultSerializer(serializers.ModelSerializer):
    student_email = serializers.EmailField(source='student.email', read_only=True)
    student_name = serializers.CharField(source='student.full_name', read_only=True)
    published_by_name = serializers.CharField(source='published_by.full_name', read_only=True)

    class Meta:
        model = Result
        fields = [
            'id', 'student', 'student_email', 'student_name', 'subject', 'semester', 
            'exam_type', 'marks_obtained', 'max_marks', 'grade', 
            'result_pdf', 'published_by', 'published_by_name', 'published_at',
            'gpa', 'has_backlog', 'backlog_subjects',
            'marks', 'remarks', 'created_at'
        ]
        read_only_fields = ['published_by']
