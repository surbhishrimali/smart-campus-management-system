from rest_framework import serializers
from certificates.models import Certificate
from accounts.models import User

class CertificateSerializer(serializers.ModelSerializer):
    student_email = serializers.EmailField(source='student.email', read_only=True)
    student_name = serializers.CharField(source='student.full_name', read_only=True)
    student = serializers.PrimaryKeyRelatedField(
        queryset=User.objects.filter(role=User.Role.STUDENT),
        required=False
    )

    class Meta:
        model = Certificate
        fields = ['id', 'student', 'student_email', 'student_name', 'title', 'issued_by', 'issue_date', 'certificate_file', 'status']
