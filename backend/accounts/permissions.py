from rest_framework.permissions import BasePermission
from accounts.models import User

class IsRole(BasePermission):
    required_roles = ()

    def has_permission(self, request, view):
        user = request.user
        if not user or not user.is_authenticated:
            return False
        return getattr(user, "role", None) in self.required_roles

class IsStudent(IsRole):
    required_roles = (User.Role.STUDENT,)

class IsFaculty(IsRole):
    required_roles = (User.Role.FACULTY,)

class IsAdmin(IsRole):
    required_roles = (User.Role.ADMIN,)
