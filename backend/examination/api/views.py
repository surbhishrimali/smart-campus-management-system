from rest_framework import viewsets, filters, permissions
from django_filters.rest_framework import DjangoFilterBackend
from rest_framework.pagination import PageNumberPagination
from examination.models import Examination
from examination.api.serializers import ExaminationSerializer
from accounts.permissions import IsAdmin

class ExaminationViewSet(viewsets.ModelViewSet):
    queryset = Examination.objects.all().order_by('exam_date', 'start_time')
    serializer_class = ExaminationSerializer
    pagination_class = PageNumberPagination
    filter_backends = [DjangoFilterBackend, filters.SearchFilter, filters.OrderingFilter]
    filterset_fields = ['semester', 'exam_type', 'exam_date']
    search_fields = ['subject']
    ordering_fields = ['exam_date', 'start_time', 'semester']

    def get_permissions(self):
        if self.action in ['create', 'update', 'partial_update', 'destroy']:
            return [permissions.IsAuthenticated(), IsAdmin()]
        return [permissions.IsAuthenticated()]
