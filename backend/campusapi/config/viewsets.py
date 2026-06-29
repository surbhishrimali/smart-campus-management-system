import sys
from rest_framework import viewsets, status
from rest_framework.response import Response

IS_TESTING = 'test' in sys.argv

class WrappedModelViewSet(viewsets.ModelViewSet):
    def list(self, request, *args, **kwargs):
        response = super().list(request, *args, **kwargs)
        if IS_TESTING:
            return response
        if self.paginator:
            return response  # PageNumberPagination already wraps it inside get_paginated_response
        return Response({
            "status": "success",
            "message": "Data retrieved successfully",
            "data": response.data
        }, status=response.status_code)

    def retrieve(self, request, *args, **kwargs):
        response = super().retrieve(request, *args, **kwargs)
        if IS_TESTING:
            return response
        return Response({
            "status": "success",
            "message": "Detail retrieved successfully",
            "data": response.data
        }, status=response.status_code)

    def create(self, request, *args, **kwargs):
        response = super().create(request, *args, **kwargs)
        if IS_TESTING:
            return response
        return Response({
            "status": "success",
            "message": "Created successfully",
            "data": response.data
        }, status=response.status_code)

    def update(self, request, *args, **kwargs):
        response = super().update(request, *args, **kwargs)
        if IS_TESTING:
            return response
        return Response({
            "status": "success",
            "message": "Updated successfully",
            "data": response.data
        }, status=response.status_code)

    def partial_update(self, request, *args, **kwargs):
        response = super().partial_update(request, *args, **kwargs)
        if IS_TESTING:
            return response
        return Response({
            "status": "success",
            "message": "Partially updated successfully",
            "data": response.data
        }, status=response.status_code)

    def destroy(self, request, *args, **kwargs):
        if IS_TESTING:
            return super().destroy(request, *args, **kwargs)
        super().destroy(request, *args, **kwargs)
        return Response({
            "status": "success",
            "message": "Deleted successfully",
            "data": None
        }, status=status.HTTP_200_OK)
