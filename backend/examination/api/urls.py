from django.urls import path, include
from rest_framework.routers import DefaultRouter
from examination.api.views import ExaminationViewSet

router = DefaultRouter()
router.register('', ExaminationViewSet, basename='examination')

urlpatterns = [
    path('', include(router.urls)),
]
