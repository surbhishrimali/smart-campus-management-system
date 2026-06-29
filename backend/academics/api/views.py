from rest_framework import permissions
from config.viewsets import WrappedModelViewSet
from academics.models import Course, Subject
from academics.api.serializers import CourseSerializer, SubjectSerializer

class CourseViewSet(WrappedModelViewSet):
    queryset = Course.objects.all().order_by('id')
    serializer_class = CourseSerializer
    permission_classes = [permissions.IsAuthenticated]

class SubjectViewSet(WrappedModelViewSet):
    queryset = Subject.objects.all().select_related('course').order_by('id')
    serializer_class = SubjectSerializer
    permission_classes = [permissions.IsAuthenticated]
    filterset_fields = ['semester', 'course']
