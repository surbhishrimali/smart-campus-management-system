from django.urls import reverse
from rest_framework import status
from rest_framework.test import APITestCase
from accounts.models import User
from faculty.models import FacultyProfile

class FacultyProfileAPITests(APITestCase):
    def setUp(self):
        self.admin_user = User.objects.create_superuser(
            email='admin@campus.edu',
            password='password123',
            full_name='Admin User'
        )
        self.faculty_user = User.objects.create_user(
            email='faculty@campus.edu',
            password='password123',
            role=User.Role.FACULTY,
            full_name='Faculty User'
        )
        self.student_user = User.objects.create_user(
            email='student@campus.edu',
            password='password123',
            role=User.Role.STUDENT,
            full_name='Student User'
        )
        
        self.faculty_profile = FacultyProfile.objects.create(
            user=self.faculty_user,
            faculty_id='FAC99',
            department='Computer Science',
            designation='Assistant Professor',
            phone='9998887776',
            specialization='Machine Learning'
        )
        
        self.list_create_url = reverse('faculty-profile-list')
        self.detail_url = reverse('faculty-profile-detail', kwargs={'pk': self.faculty_profile.pk})

    def get_jwt_header(self, user):
        response = self.client.post(reverse('token_obtain_pair'), {
            'email': user.email,
            'password': 'password123'
        })
        return f"Bearer {response.data['access']}"

    def test_faculty_retrieve_own_profile(self):
        auth_header = self.get_jwt_header(self.faculty_user)
        self.client.credentials(HTTP_AUTHORIZATION=auth_header)
        response = self.client.get(self.detail_url)
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(response.data['faculty_id'], 'FAC99')

    def test_student_cannot_view_faculty_profile(self):
        auth_header = self.get_jwt_header(self.student_user)
        self.client.credentials(HTTP_AUTHORIZATION=auth_header)
        response = self.client.get(self.detail_url)
        self.assertEqual(response.status_code, status.HTTP_403_FORBIDDEN)

    def test_admin_can_create_faculty_profile(self):
        new_fac = User.objects.create_user(
            email='newfac@campus.edu',
            password='password123',
            role=User.Role.FACULTY,
            full_name='New Faculty'
        )
        auth_header = self.get_jwt_header(self.admin_user)
        self.client.credentials(HTTP_AUTHORIZATION=auth_header)
        data = {
            'user': new_fac.id,
            'faculty_id': 'FAC01',
            'department': 'Electrical Engineering',
            'designation': 'HOD',
            'phone': '1112223334',
            'specialization': 'Signal Processing'
        }
        response = self.client.post(self.list_create_url, data)
        self.assertEqual(response.status_code, status.HTTP_201_CREATED)
