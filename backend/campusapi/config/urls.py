"""
URL configuration for config project.

The `urlpatterns` list routes URLs to views. For more information please see:
    https://docs.djangoproject.com/en/6.0/topics/http/urls/
Examples:
Function views
    1. Add an import:  from my_app import views
    2. Add a URL to urlpatterns:  path('', views.home, name='home')
Class-based views
    1. Add an import:  from other_app.views import Home
    2. Add a URL to urlpatterns:  path('', Home.as_view(), name='home')
Including another URLconf
    1. Import the include() function: from django.urls import include, path
    2. Add a URL to urlpatterns:  path('blog/', include('blog.urls'))
"""
from django.contrib import admin
from django.urls import path, include
from drf_spectacular.views import SpectacularAPIView, SpectacularSwaggerView
from rest_framework.routers import DefaultRouter

from accounts.api.views import UserViewSet

from rest_framework.routers import DefaultRouter
from resources.api.views import NoteViewSet, PyqViewSet, YoutubeRecommendationViewSet

user_router = DefaultRouter()
user_router.register('', UserViewSet, basename='user')

router_compat = DefaultRouter()
router_compat.register('notes', NoteViewSet, basename='compat-note')
router_compat.register('pyqs', PyqViewSet, basename='compat-pyq')
router_compat.register('youtube-recommendations', YoutubeRecommendationViewSet, basename='compat-youtube')

urlpatterns = [
    path('', lambda request: None),
    path('admin/', admin.site.urls),

    # OpenAPI schema + Swagger UI
    path('api/schema/', SpectacularAPIView.as_view(), name='api-schema'),
    path('api/docs/', SpectacularSwaggerView.as_view(url_name='api-schema'), name='api-docs'),

    # API root
    path('api/auth/', include('accounts.api.urls')),
    path('api/accounts/', include('accounts.api.urls')),
    path('api/users/', include(user_router.urls)),

    path('api/students/', include('students.api.urls')),
    path('api/faculty/', include('faculty.api.urls')),
    path('api/admin/', include('adminpanel.api.urls')),

    path('api/attendance/', include('attendance.api.urls')),
    path('api/results/', include('results.api.urls')),
    path('api/certificates/', include('certificates.api.urls')),
    path('api/resources/', include('resources.api.urls')),
    path('api/notifications/', include('notifications.api.urls')),
    path('api/complaints/', include('complaints.api.urls')),
    path('api/timetable/', include('timetable.api.urls')),
    path('api/examination/', include('examination.api.urls')),
    path('api/', include('academics.api.urls')),
    path('api/', include(router_compat.urls)),
]

from django.conf import settings
from django.conf.urls.static import static

if settings.DEBUG:
    urlpatterns += static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)

