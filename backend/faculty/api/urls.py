from django.urls import path, include
from rest_framework.routers import DefaultRouter
from faculty.api.views import FacultyProfileViewSet

router = DefaultRouter()
router.register('', FacultyProfileViewSet, basename='faculty-profile')

urlpatterns = [
    path('', include(router.urls)),
]
