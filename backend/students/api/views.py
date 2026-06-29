from rest_framework import filters, permissions
from django_filters.rest_framework import DjangoFilterBackend
from students.models import StudentProfile
from students.api.serializers import StudentProfileSerializer
from accounts.permissions import IsAdmin, IsFaculty, IsStudent
from config.viewsets import WrappedModelViewSet

class StudentProfileViewSet(WrappedModelViewSet):
    queryset = StudentProfile.objects.all().select_related('user').order_by('id')
    serializer_class = StudentProfileSerializer
    filter_backends = [DjangoFilterBackend, filters.SearchFilter, filters.OrderingFilter]
    filterset_fields = ['user', 'branch', 'semester', 'section']
    search_fields = ['enrollment_number', 'user__full_name', 'user__email']
    ordering_fields = ['gpa', 'semester', 'enrollment_number']
    def get_permissions(self):
        if self.action in ['create', 'destroy']:
            return [permissions.IsAuthenticated(), (IsAdmin | IsFaculty)()]
        return [permissions.IsAuthenticated()]

    def get_queryset(self):
        user = self.request.user
        if user.is_authenticated and user.role == 'STUDENT':
            return StudentProfile.objects.filter(user=user).order_by('id')
        return super().get_queryset()
