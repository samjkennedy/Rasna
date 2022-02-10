int main() {

    int x = 5;
    int y = (x <= 2) ? 0 : (x > 5) ? 10 : 5;
    printf("%d\n", y);
}