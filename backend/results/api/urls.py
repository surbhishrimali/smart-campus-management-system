from django.urls import path, include
from rest_framework.routers import DefaultRouter
from results.api.views import ResultViewSet

router = DefaultRouter()
router.register('', ResultViewSet, basename='result')

urlpatterns = [
    path('', include(router.urls)),
]
