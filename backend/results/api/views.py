from rest_framework import filters, permissions, status
from rest_framework.decorators import action
from rest_framework.response import Response
from django_filters.rest_framework import DjangoFilterBackend
from results.models import Result
from results.api.serializers import ResultSerializer
from accounts.permissions import IsAdmin, IsFaculty
from config.viewsets import WrappedModelViewSet

class ResultViewSet(WrappedModelViewSet):
    queryset = Result.objects.all().select_related('student', 'published_by').order_by('id')
    serializer_class = ResultSerializer
    filter_backends = [DjangoFilterBackend, filters.SearchFilter, filters.OrderingFilter]
    filterset_fields = ['semester', 'subject', 'student']
    search_fields = ['student__full_name', 'student__email', 'subject']
    ordering_fields = ['semester', 'marks_obtained', 'marks']

    def get_permissions(self):
        if self.action in ['create', 'update', 'partial_update', 'destroy', 'upload', 'update_result']:
            return [permissions.IsAuthenticated(), (IsAdmin | IsFaculty)()]
        return [permissions.IsAuthenticated()]

    def get_queryset(self):
        user = self.request.user
        if user.is_authenticated and user.role == 'STUDENT':
            return Result.objects.filter(student=user).order_by('id')
        return super().get_queryset()

    def perform_create(self, serializer):
        serializer.save(published_by=self.request.user)

    @action(detail=False, methods=['post'], url_path='upload')
    def upload(self, request):
        serializer = self.get_serializer(data=request.data)
        serializer.is_valid(raise_exception=True)
        serializer.save(published_by=request.user)
        return Response({
            "status": "success",
            "message": "Result uploaded successfully",
            "data": serializer.data
        }, status=status.HTTP_201_CREATED)

    @action(detail=False, methods=['put'], url_path='update')
    def update_result(self, request):
        result_id = request.data.get('id')
        if not result_id:
            return Response({
                "status": "error",
                "message": "Result ID is required in the request body",
                "data": None
            }, status=status.HTTP_400_BAD_REQUEST)
        
        try:
            instance = Result.objects.get(id=result_id)
        except Result.DoesNotExist:
            return Response({
                "status": "error",
                "message": "Result record not found",
                "data": None
            }, status=status.HTTP_404_NOT_FOUND)

        serializer = self.get_serializer(instance, data=request.data, partial=True)
        serializer.is_valid(raise_exception=True)
        serializer.save()
        return Response({
            "status": "success",
            "message": "Result updated successfully",
            "data": serializer.data
        }, status=status.HTTP_200_OK)

    @action(detail=False, methods=['get'], url_path='student')
    def student_results(self, request):
        user = request.user
        if user.role != 'STUDENT':
            return Response({
                "status": "error",
                "message": "Only students can view their own results",
                "data": None
            }, status=status.HTTP_403_FORBIDDEN)
            
        queryset = Result.objects.filter(student=user).order_by('id')
        page = self.paginate_queryset(queryset)
        if page is not None:
            serializer = self.get_serializer(page, many=True)
            return self.get_paginated_response(serializer.data)
            
        serializer = self.get_serializer(queryset, many=True)
        return Response({
            "status": "success",
            "message": "Student results retrieved successfully",
            "data": serializer.data
        }, status=status.HTTP_200_OK)
