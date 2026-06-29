from django.urls import path
from adminpanel.api.views import AdminDashboardSummaryView

urlpatterns = [
    path('summary/', AdminDashboardSummaryView.as_view(), name='admin-summary'),
]


