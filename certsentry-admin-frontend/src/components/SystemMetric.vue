<template>
    <v-card elevation="5"  height="125" class="ma-2 pa-2">
        <v-list-item three-line>
            <v-list-item-content>
                <div class="overline mb-4">{{title}}</div>
                <v-list-item-title class="headline mb-1 ">{{metric}}</v-list-item-title>
                <v-list-item-subtitle>{{msg}}</v-list-item-subtitle>
            </v-list-item-content>
            <v-list-item-avatar tile>
                <v-icon x-large left color="green">{{icon}}</v-icon>
            </v-list-item-avatar>
        </v-list-item>
    </v-card>
</template>
<script>
export default {
    name: 'SystemMetric',
    props: [ 'msg','icon','title','metric' ],
}
</script>