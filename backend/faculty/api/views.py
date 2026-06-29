from rest_framework import viewsets, filters, permissions
from django_filters.rest_framework import DjangoFilterBackend
from rest_framework.pagination import PageNumberPagination
from faculty.models import FacultyProfile
from faculty.api.serializers import FacultyProfileSerializer
from accounts.permissions import IsAdmin, IsFaculty

class FacultyProfileViewSet(viewsets.ModelViewSet):
    queryset = FacultyProfile.objects.all().select_related('user')
    serializer_class = FacultyProfileSerializer
    pagination_class = PageNumberPagination
    filter_backends = [DjangoFilterBackend, filters.SearchFilter, filters.OrderingFilter]
    filterset_fields = ['department', 'designation']
    search_fields = ['faculty_id', 'user__full_name', 'user__email']
    ordering_fields = ['faculty_id', 'department']

    def get_permissions(self):
        return [permissions.IsAuthenticated(), (IsAdmin | IsFaculty)()]

    def get_queryset(self):
        user = self.request.user
        if user.is_authenticated and user.role == 'FACULTY':
            return FacultyProfile.objects.filter(user=user)
        return super().get_queryset()
