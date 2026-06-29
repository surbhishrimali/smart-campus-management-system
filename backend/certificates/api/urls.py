from django.urls import path, include
from rest_framework.routers import DefaultRouter
from certificates.api.views import CertificateViewSet

router = DefaultRouter()
router.register('', CertificateViewSet, basename='certificate')

urlpatterns = [
    path('', include(router.urls)),
]
