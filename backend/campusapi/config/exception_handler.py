from rest_framework.views import exception_handler
from rest_framework.response import Response

def custom_exception_handler(exc, context):
    # Call REST framework's default exception handler first to get the standard error response
    response = exception_handler(exc, context)
    
    if response is not None:
        # Standard DRF exceptions (like validation error, authentication error, etc.)
        err_data = response.data
        message = "An error occurred"
        
        if isinstance(err_data, dict):
            if 'detail' in err_data:
                message = str(err_data['detail'])
            else:
                # Format validation errors nicely into a single readable string
                message = "; ".join([f"{k}: {v}" for k, v in err_data.items()])
        elif isinstance(err_data, list):
            message = "; ".join([str(x) for x in err_data])

        response.data = {
            "status": "error",
            "message": message,
            "data": err_data
        }
    else:
        # Unhandled Django or Python exception (translates to 500 server error)
        from django.conf import settings
        import traceback
        
        message = str(exc) if settings.DEBUG else "Internal server error occurred"
        
        response = Response({
            "status": "error",
            "message": message,
            "data": traceback.format_exc() if settings.DEBUG else None
        }, status=500)
        
    return response
