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
    certificate_url = serializers.SerializerMethodField()

    class Meta:
        model = Certificate
        fields = ['id', 'student', 'student_email', 'student_name', 'title', 'issued_by', 'issue_date', 'certificate_file', 'certificate_url', 'status']

    def get_certificate_url(self, obj):
        if obj.certificate_file:
            request = self.context.get('request')
            if request:
                return request.build_absolute_uri(obj.certificate_file.url)
            return obj.certificate_file.url
        return None
