from django.urls import path, include
from rest_framework.routers import DefaultRouter
from academics.api.views import CourseViewSet, SubjectViewSet

router = DefaultRouter()
router.register('courses', CourseViewSet, basename='course')
router.register('subjects', SubjectViewSet, basename='subject')

urlpatterns = [
    path('', include(router.urls)),
]
