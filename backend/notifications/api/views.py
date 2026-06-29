from rest_framework import filters, permissions, status
from rest_framework.decorators import action
from rest_framework.response import Response
from django.db import models as django_models
from django_filters.rest_framework import DjangoFilterBackend
from notifications.models import Notification
from notifications.api.serializers import NotificationSerializer
from accounts.permissions import IsAdmin, IsFaculty
from config.viewsets import WrappedModelViewSet

class NotificationViewSet(WrappedModelViewSet):
    queryset = Notification.objects.all().select_related('sender').order_by('id')
    serializer_class = NotificationSerializer
    filter_backends = [DjangoFilterBackend, filters.SearchFilter, filters.OrderingFilter]
    filterset_fields = ['target_role', 'is_active', 'notification_type']
    search_fields = ['title', 'content', 'message', 'notification_type']
    ordering_fields = ['created_at']

    def get_permissions(self):
        if self.action in ['create', 'update', 'partial_update', 'destroy', 'create_notification']:
            return [permissions.IsAuthenticated(), (IsAdmin | IsFaculty)()]
        return [permissions.IsAuthenticated()]

    def get_queryset(self):
        user = self.request.user
        if not user.is_authenticated:
            return Notification.objects.none()
        if user.role in ['ADMIN', 'FACULTY']:
            return super().get_queryset()
        # Students see only active notifications targeted to them or ALL
        return Notification.objects.filter(is_active=True).filter(
            django_models.Q(target_role='ALL') | django_models.Q(target_role='STUDENT')
        ).order_by('id')

    def perform_create(self, serializer):
        serializer.save(sender=self.request.user)

    @action(detail=False, methods=['post'], url_path='create')
    def create_notification(self, request):
        serializer = self.get_serializer(data=request.data)
        serializer.is_valid(raise_exception=True)
        serializer.save(sender=request.user)
        return Response({
            "status": "success",
            "message": "Notification created successfully",
            "data": serializer.data
        }, status=status.HTTP_201_CREATED)
