from rest_framework import filters, permissions, status
from rest_framework.decorators import action
from rest_framework.response import Response
from django_filters.rest_framework import DjangoFilterBackend
from resources.models import Resource
from resources.api.serializers import ResourceSerializer
from resources.api.serializers_compat import NoteSerializer, PyqSerializer
from accounts.permissions import IsAdmin, IsFaculty
from config.viewsets import WrappedModelViewSet

class ResourceViewSet(WrappedModelViewSet):
    queryset = Resource.objects.all().select_related('uploaded_by').order_by('id')
    serializer_class = ResourceSerializer
    filter_backends = [DjangoFilterBackend, filters.SearchFilter, filters.OrderingFilter]
    filterset_fields = ['resource_type', 'department', 'subject']
    search_fields = ['title', 'description', 'department', 'subject']
    ordering_fields = ['created_at', 'uploaded_at']

    def get_permissions(self):
        if self.action in ['create', 'update', 'partial_update', 'destroy', 'upload']:
            return [permissions.IsAuthenticated(), (IsAdmin | IsFaculty)()]
        return [permissions.IsAuthenticated()]

    def perform_create(self, serializer):
        serializer.save(uploaded_by=self.request.user)

    @action(detail=False, methods=['post'], url_path='upload')
    def upload(self, request):
        serializer = self.get_serializer(data=request.data)
        serializer.is_valid(raise_exception=True)
        serializer.save(uploaded_by=request.user)
        return Response({
            "status": "success",
            "message": "Resource uploaded successfully",
            "data": serializer.data
        }, status=status.HTTP_201_CREATED)

class NoteViewSet(WrappedModelViewSet):
    queryset = Resource.objects.filter(resource_type='NOTE').select_related('uploaded_by').order_by('id')
    serializer_class = NoteSerializer
    permission_classes = [permissions.IsAuthenticated]

    def perform_create(self, serializer):
        serializer.save(uploaded_by=self.request.user, resource_type='NOTE')

class PyqViewSet(WrappedModelViewSet):
    queryset = Resource.objects.filter(resource_type='PYQ').select_related('uploaded_by').order_by('id')
    serializer_class = PyqSerializer
    permission_classes = [permissions.IsAuthenticated]

    def perform_create(self, serializer):
        serializer.save(uploaded_by=self.request.user, resource_type='PYQ')
