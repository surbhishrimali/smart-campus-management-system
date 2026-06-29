from rest_framework import filters, permissions, status
from rest_framework.decorators import action
from rest_framework.response import Response
from django_filters.rest_framework import DjangoFilterBackend
from attendance.models import Attendance
from attendance.api.serializers import AttendanceSerializer
from accounts.permissions import IsAdmin, IsFaculty
from config.viewsets import WrappedModelViewSet

class AttendanceViewSet(WrappedModelViewSet):
    queryset = Attendance.objects.all().select_related('student', 'faculty').order_by('id')
    serializer_class = AttendanceSerializer
    filter_backends = [DjangoFilterBackend, filters.SearchFilter, filters.OrderingFilter]
    filterset_fields = ['subject', 'date', 'status', 'student']
    search_fields = ['student__full_name', 'student__email', 'subject']
    ordering_fields = ['date', 'student']

    def get_permissions(self):
        if self.action in ['create', 'update', 'partial_update', 'destroy', 'mark', 'update_attendance']:
            return [permissions.IsAuthenticated(), (IsAdmin | IsFaculty)()]
        return [permissions.IsAuthenticated()]

    def get_queryset(self):
        user = self.request.user
        if user.is_authenticated and user.role == 'STUDENT':
            return Attendance.objects.filter(student=user).order_by('id')
        return super().get_queryset()

    def perform_create(self, serializer):
        serializer.save(faculty=self.request.user)

    @action(detail=False, methods=['post'], url_path='mark')
    def mark(self, request):
        serializer = self.get_serializer(data=request.data)
        serializer.is_valid(raise_exception=True)
        serializer.save(faculty=request.user)
        return Response({
            "status": "success",
            "message": "Attendance marked successfully",
            "data": serializer.data
        }, status=status.HTTP_201_CREATED)

    @action(detail=False, methods=['put'], url_path='update')
    def update_attendance(self, request):
        attendance_id = request.data.get('id')
        if not attendance_id:
            return Response({
                "status": "error",
                "message": "Attendance ID is required in the request body",
                "data": None
            }, status=status.HTTP_400_BAD_REQUEST)
        
        try:
            instance = Attendance.objects.get(id=attendance_id)
        except Attendance.DoesNotExist:
            return Response({
                "status": "error",
                "message": "Attendance record not found",
                "data": None
            }, status=status.HTTP_404_NOT_FOUND)

        serializer = self.get_serializer(instance, data=request.data, partial=True)
        serializer.is_valid(raise_exception=True)
        serializer.save()
        return Response({
            "status": "success",
            "message": "Attendance updated successfully",
            "data": serializer.data
        }, status=status.HTTP_200_OK)

    @action(detail=False, methods=['get'], url_path='student')
    def student_attendance(self, request):
        user = request.user
        if user.role != 'STUDENT':
            return Response({
                "status": "error",
                "message": "Only students can view their own attendance",
                "data": None
            }, status=status.HTTP_403_FORBIDDEN)
            
        queryset = Attendance.objects.filter(student=user).order_by('id')
        page = self.paginate_queryset(queryset)
        if page is not None:
            serializer = self.get_serializer(page, many=True)
            return self.get_paginated_response(serializer.data)
            
        serializer = self.get_serializer(queryset, many=True)
        return Response({
            "status": "success",
            "message": "Student attendance retrieved successfully",
            "data": serializer.data
        }, status=status.HTTP_200_OK)
