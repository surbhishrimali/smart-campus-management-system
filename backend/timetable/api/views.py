from rest_framework import viewsets, filters, permissions
from django_filters.rest_framework import DjangoFilterBackend
from rest_framework.pagination import PageNumberPagination
from timetable.models import Timetable
from timetable.api.serializers import TimetableSerializer
from accounts.permissions import IsAdmin

class TimetableViewSet(viewsets.ModelViewSet):
    queryset = Timetable.objects.all().select_related('faculty').order_by('id')
    serializer_class = TimetableSerializer
    pagination_class = PageNumberPagination
    filter_backends = [DjangoFilterBackend, filters.SearchFilter, filters.OrderingFilter]
    filterset_fields = ['semester', 'day', 'faculty']
    search_fields = ['subject', 'room_no']
    ordering_fields = ['start_time', 'semester']

    def get_permissions(self):
        if self.action in ['create', 'update', 'partial_update', 'destroy']:
            return [permissions.IsAuthenticated(), IsAdmin()]
        return [permissions.IsAuthenticated()]
