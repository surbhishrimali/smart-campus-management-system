from django.urls import path, include
from rest_framework.routers import DefaultRouter
from students.api.views import StudentProfileViewSet

router = DefaultRouter()
router.register('', StudentProfileViewSet, basename='student-profile')

urlpatterns = [
    path('', include(router.urls)),
]
