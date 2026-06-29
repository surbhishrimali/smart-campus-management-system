from django.urls import reverse
from rest_framework import status
from rest_framework.test import APITestCase
from accounts.models import User

class AccountAPITests(APITestCase):
    def setUp(self):
        self.user = User.objects.create_user(
            email='testuser@campus.edu',
            password='password123',
            role=User.Role.STUDENT,
            full_name='Test User'
        )
        self.login_url = reverse('login')
        self.profile_url = reverse('profile')

    def test_login_success(self):
        data = {
            'email': 'testuser@campus.edu',
            'password': 'password123'
        }
        response = self.client.post(self.login_url, data)
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertIn('access', response.data)
        self.assertIn('refresh', response.data)
        self.assertEqual(response.data['user']['email'], 'testuser@campus.edu')

    def test_login_invalid_credentials(self):
        data = {
            'email': 'testuser@campus.edu',
            'password': 'wrongpassword'
        }
        response = self.client.post(self.login_url, data)
        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)

    def test_profile_retrieval_unauthenticated(self):
        response = self.client.get(self.profile_url)
        self.assertEqual(response.status_code, status.HTTP_401_UNAUTHORIZED)

    def test_profile_retrieval_authenticated(self):
        # Obtain JWT token via login view
        login_response = self.client.post(self.login_url, {
            'email': 'testuser@campus.edu',
            'password': 'password123'
        })
        token = login_response.data['access']
        
        self.client.credentials(HTTP_AUTHORIZATION=f"Bearer {token}")
        response = self.client.get(self.profile_url)
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(response.data['full_name'], 'Test User')

    def test_profile_patch_authenticated(self):
        login_response = self.client.post(self.login_url, {
            'email': 'testuser@campus.edu',
            'password': 'password123'
        })
        token = login_response.data['access']
        
        self.client.credentials(HTTP_AUTHORIZATION=f"Bearer {token}")
        data = {
            'full_name': 'Updated Test User'
        }
        response = self.client.patch(self.profile_url, data)
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(response.data['full_name'], 'Updated Test User')
        
        # Verify in DB
        self.user.refresh_from_db()
        self.assertEqual(self.user.full_name, 'Updated Test User')
