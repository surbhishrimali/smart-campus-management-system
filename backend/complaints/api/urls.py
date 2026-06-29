from django.urls import path, include
from rest_framework.routers import DefaultRouter
from complaints.api.views import ComplaintViewSet

router = DefaultRouter()
router.register('', ComplaintViewSet, basename='complaint')

urlpatterns = [
    path('', include(router.urls)),
]
