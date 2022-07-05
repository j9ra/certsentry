<template>
  <v-app class="grey lighten-3">
   
    
    <app-menu :drawer="drawer"></app-menu>
  

    <v-app-bar app color="grey lighten-5">
      <v-app-bar-nav-icon @click.stop="drawer = !drawer"></v-app-bar-nav-icon>
      <v-toolbar-title>Cert Sentry</v-toolbar-title>
    </v-app-bar>


    <v-main>
    <v-container fluid class="pa-0">
       
      <router-view></router-view>
       
    </v-container>
    </v-main>


    <v-footer color="grey lighten-2" app>
      <span>&copy; 2020 (J.Grabowski)</span>
    </v-footer>

  </v-app>
</template>

<script>
import AppMenu from '@/components/AppMenu.vue'


export default {
  name: 'App',

  

  data: () => ({
    drawer: true,
  }),
  components:  { AppMenu,}
};
</script>
