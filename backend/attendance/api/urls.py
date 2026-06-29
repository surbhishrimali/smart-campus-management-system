from django.urls import path, include
from rest_framework.routers import DefaultRouter
from attendance.api.views import AttendanceViewSet

router = DefaultRouter()
router.register('', AttendanceViewSet, basename='attendance')

urlpatterns = [
    path('', include(router.urls)),
]
