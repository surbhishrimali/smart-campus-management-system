from django.urls import path, include
from rest_framework.routers import DefaultRouter
from resources.api.views import ResourceViewSet

router = DefaultRouter()
router.register('', ResourceViewSet, basename='resource')

urlpatterns = [
    path('', include(router.urls)),
]
