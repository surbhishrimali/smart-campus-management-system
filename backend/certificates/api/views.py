from rest_framework import viewsets, filters, permissions
from django_filters.rest_framework import DjangoFilterBackend
from rest_framework.pagination import PageNumberPagination
from certificates.models import Certificate
from certificates.api.serializers import CertificateSerializer
from accounts.permissions import IsAdmin, IsFaculty

class CertificateViewSet(viewsets.ModelViewSet):
    queryset = Certificate.objects.all().select_related('student')
    serializer_class = CertificateSerializer
    pagination_class = PageNumberPagination
    filter_backends = [DjangoFilterBackend, filters.SearchFilter, filters.OrderingFilter]
    filterset_fields = ['status', 'student']
    search_fields = ['title', 'student__full_name', 'student__email']
    ordering_fields = ['issue_date', 'status']

    def get_permissions(self):
        if self.action in ['create']:
            return [permissions.IsAuthenticated()]
        if self.action in ['update', 'partial_update', 'destroy']:
            return [permissions.IsAuthenticated(), (IsAdmin | IsFaculty)()]
        return [permissions.IsAuthenticated()]

    def get_queryset(self):
        user = self.request.user
        if not user.is_authenticated:
            return Certificate.objects.none()
        if user.role in ['ADMIN', 'FACULTY']:
            return super().get_queryset()
        return Certificate.objects.filter(student=user)

    def perform_create(self, serializer):
        user = self.request.user
        if user.role == 'STUDENT':
            serializer.save(student=user, status='PENDING')
        else:
            serializer.save()
