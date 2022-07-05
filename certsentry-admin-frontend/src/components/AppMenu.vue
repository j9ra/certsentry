<template>
    <v-navigation-drawer :value="cdrawer" color="grey darken-4" dark app>
     <v-list-item>
        <v-list-item-content>
          <v-list-item-title class="title">
            Admin Panel
          </v-list-item-title>
        </v-list-item-content>
      </v-list-item>
      <v-divider></v-divider>
      <v-list>
        <v-list-item link to="/">
          <v-list-item-action>
            <v-icon>mdi-home</v-icon>
          </v-list-item-action>
          <v-list-item-content>
            <v-list-item-title>Home</v-list-item-title>
          </v-list-item-content>
        </v-list-item>
        <v-list-item link to="/users/list">
          <v-list-item-action>
            <v-icon>mdi-account-details</v-icon>
          </v-list-item-action>
          <v-list-item-content>
            <v-list-item-title>Users</v-list-item-title>
          </v-list-item-content>
        </v-list-item>
        <v-list-item link to="/profiles/list">
          <v-list-item-action>
            <v-icon>mdi-tag-multiple</v-icon>
          </v-list-item-action>
          <v-list-item-content>
            <v-list-item-title>Profiles</v-list-item-title>
          </v-list-item-content>
        </v-list-item>
        <v-list-item link to="/trustlists">
          <v-list-item-action>
            <v-icon>mdi-newspaper-variant-multiple-outline</v-icon>
          </v-list-item-action>
          <v-list-item-content>
            <v-list-item-title>Trust Lists</v-list-item-title>
          </v-list-item-content>
        </v-list-item>
        <v-list-item link to="/events">
          <v-list-item-action>
            <v-icon>mdi-notebook-outline</v-icon>
          </v-list-item-action>
          <v-list-item-content>
            <v-list-item-title>Events</v-list-item-title>
          </v-list-item-content>
        </v-list-item>
      </v-list>
    </v-navigation-drawer>
</template>

<script>
export default {
    name: 'AppMenu',
    props: ['drawer'],

    computed: {
        cdrawer: function () {
            return this.drawer
        }
    }
}
</script>