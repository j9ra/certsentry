import Vue from 'vue'
import VueRouter from 'vue-router'
import Home from '../views/Home.vue'
import Users from '../views/Users.vue'
import Profiles from '../views/Profiles.vue'
import NewUser from '../views/NewUser.vue'
import UsersList from '../views/UsersList.vue'
import NewProfile from '../views/NewProfile.vue'
import ProfilesList from '../views/ProfilesList.vue'
import TrustLists from '../views/TrustLists.vue'
import Events from '../views/Events.vue'
import Login from '../views/Login.vue'

import store from '../store'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home,
  },
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: {
      login: true
    }
  },
  {
    path: '/users/:id',
    component: Users,
    children: [
      { 
        path: '',
        name: 'Users',
        component: UsersList,
      },
      {
         path: 'new',
         name: 'NewUser',
         component: NewUser,
      },
      {
        path: 'edit',
        name: 'EditUser',
        component: NewUser,
     }
    ]
  },
  {
    path: '/profiles/:id',
    component: Profiles,
    children: [
      { 
        path: '',
        name: 'Profiles',
        component: ProfilesList, 
      },
      { 
        path: 'new',
        name: 'NewProfile',
        component: NewProfile,
      },
      { 
        path: 'edit',
        name: 'EditProfile',
        component: NewProfile,
      },
    ]
  },
  {
      path: '/trustlists',
      name: 'TrustLists',
      component: TrustLists,
  },
  {
    path: '/events',
    name: 'Events',
    component: Events,

  }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})


router.beforeEach((to, from, next) => {
   if (!to.matched.some(record => record.meta.login)) {
      if (!store.getters.isLoggedIn) {

        console.log(`redirect to login page, islogged: ${store.getters.isLoggedIn}`);
          next({
              path: '/login'
          })
      } else {
          next();
      }
   } else {
       next(); // make sure to always call next()!
   }
});


export default router
