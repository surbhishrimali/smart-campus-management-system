from rest_framework import viewsets, filters, permissions
from django_filters.rest_framework import DjangoFilterBackend
from rest_framework.pagination import PageNumberPagination
from complaints.models import Complaint
from complaints.api.serializers import ComplaintSerializer
from accounts.permissions import IsAdmin
from django.utils import timezone

class ComplaintViewSet(viewsets.ModelViewSet):
    queryset = Complaint.objects.all().select_related('user', 'resolved_by')
    serializer_class = ComplaintSerializer
    pagination_class = PageNumberPagination
    filter_backends = [DjangoFilterBackend, filters.SearchFilter, filters.OrderingFilter]
    filterset_fields = ['status', 'priority', 'user']
    search_fields = ['title', 'description']
    ordering_fields = ['created_at', 'priority']

    def get_permissions(self):
        if self.action in ['update', 'partial_update', 'destroy']:
            return [permissions.IsAuthenticated(), IsAdmin()]
        return [permissions.IsAuthenticated()]

    def get_queryset(self):
        user = self.request.user
        if not user.is_authenticated:
            return Complaint.objects.none()
        if user.role == 'ADMIN':
            return super().get_queryset()
        return Complaint.objects.filter(user=user)

    def perform_create(self, serializer):
        serializer.save(user=self.request.user)

    def perform_update(self, serializer):
        status_val = serializer.validated_data.get('status')
        resolved_at_val = None
        resolved_by_val = None
        if status_val == 'resolved':
            resolved_at_val = timezone.now()
            resolved_by_val = self.request.user
        serializer.save(resolved_by=resolved_by_val, resolved_at=resolved_at_val)
